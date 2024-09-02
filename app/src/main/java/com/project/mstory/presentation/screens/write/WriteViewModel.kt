package com.project.mstory.presentation.screens.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mstory.data.mongo.database.ImageToDeleteDao
import com.mstory.data.mongo.database.ImageToUploadDao
import com.mstory.data.mongo.database.entity.ImageToDelete
import com.mstory.data.mongo.database.entity.ImageToUpload
import com.mstory.data.mongo.repository.MongoDB
import com.mstory.ui.GalleryImage
import com.mstory.ui.GalleryState
import com.project.mstory.util.Constant
import com.project.mstory.util.RequestState
import com.project.mstory.util.fetchImagesFromFirebase
import com.project.mstory.util.model.Mood
import com.project.mstory.util.model.Story
import com.project.mstory.util.toRealmInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao,
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set
    val galleryState = GalleryState()
    init {
        getStoryIdArgument()
        fetchSelectedStory()
    }

    fun getStoryIdArgument() {
        uiState = uiState.copy(
            selectedStoryId = savedStateHandle.get<String>(
                key = Constant.WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    fun saveStory(
        story: Story,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            if(uiState.selectedStoryId != null){
                updateStory(story = story, onError = onError, onSuccess = onSuccess)
            }else{
                insertStory(story = story, onError = onError, onSuccess = onSuccess)
            }
        }
    }

    fun fetchSelectedStory() {
        if (uiState.selectedStoryId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDB.getSelectedStory(storyId = ObjectId.invoke(uiState.selectedStoryId!!))
                    .catch {
                        emit(RequestState.Error(Exception("Story is already deleted")))
                    }
                    .collect { story ->
                        if (story is RequestState.Success) {
                            setSelectedStory(story = story.data)
                            setTitle(title = story.data.title)
                            setDescription(description = story.data.description)
                            setMood(mood = Mood.valueOf(story.data.mood))

                            fetchImagesFromFirebase(
                                images = story.data.images,
                                onImageDownload = {downloadedImage ->
                                    galleryState.addImage(
                                        GalleryImage(
                                            image = downloadedImage,
                                            remoteImagePath = extractImagePath(
                                                fullImageUrl = downloadedImage.toString()
                                            )
                                        )
                                    )

                                }
                            )
                        }
                    }
            }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"

    }

    private suspend fun updateStory(
        story: Story,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ){
       val result = MongoDB.updateStory(story = story.apply {
           _id = ObjectId.invoke(uiState.selectedStoryId!!)
           date = if (uiState.updatedDateTime != null) {
               uiState.updatedDateTime!!
           } else {
               uiState.selectedStory!!.date
           }
       })
       if(result is RequestState.Success){
           uploadImagesToFirebase()
           deleteImagesFromFirebase()
           withContext(Dispatchers.Main){
               onSuccess()
           }
       }else if(result is RequestState.Error){
           withContext(Dispatchers.Main){
               onError(result.error.message.toString())
           }
       }

    }

    private fun setSelectedStory(story: Story) {
        uiState = uiState.copy(selectedStory = story)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

     fun updateDateTime(zonedDateTime: ZonedDateTime) {
         uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }


    private fun insertStory(
        story: Story,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = MongoDB.insertNewStory(story = story.apply {
                if(uiState.updatedDateTime != null){
                    date = uiState.updatedDateTime!!
                }
            })
            if (result is RequestState.Success) {
                uploadImagesToFirebase()
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else if (result is RequestState.Error) {
                withContext(Dispatchers.Main) {
                    onError(result.error.message.toString())
                }
            }
        }
    }

    fun deleteStory(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ){
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedStoryId != null) {
                val result = MongoDB.deleteStory(id = ObjectId.invoke(uiState.selectedStoryId!!))
                if (result is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        uiState.selectedStory?.let { deleteImagesFromFirebase(images = it.images) }
                        onSuccess()
                    }
                } else if (result is RequestState.Error) {
                    withContext(Dispatchers.Main) {
                        onError(result.error.message.toString())
                    }
                }
            }
        }
    }

    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.${imageType}"

        Log.d("WriteViewmodel", remoteImagePath)
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase(){
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if(sessionUri != null){
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        } else {
            galleryState.imagesToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        }
    }
}

data class UiState(
    val selectedStoryId: String? = null,
    val selectedStory: Story? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null,
)