package com.project.mstory.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mstory.data.mongo.database.ImageToDeleteDao
import com.mstory.data.mongo.database.entity.ImageToDelete
import com.mstory.data.mongo.repository.MongoDB
import com.mstory.data.mongo.repository.Stories
import com.project.mstory.util.connectActivity.ConnectObserver
import com.project.mstory.util.connectActivity.NetworkConnectivityObserve
import com.project.mstory.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivityObserve: NetworkConnectivityObserve,
    private val imageToDeleteDao: ImageToDeleteDao
): ViewModel() {

    private lateinit var allStoriesJob: Job
    private lateinit var filteredStoriesJob: Job
    var stories: MutableState<Stories> = mutableStateOf(RequestState.Idle)
    private var network by mutableStateOf(ConnectObserver.Status.UnAvailable)
    var dateIsSelected by mutableStateOf(false)
        private set

    var username by mutableStateOf("")
        private set

    init {
        getStories()
        viewModelScope.launch {
            connectivityObserve.observe().collect{
                network = it
            }
        }
    }

    fun getStories(zonedDateTime: ZonedDateTime? = null) {
        username = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        dateIsSelected = zonedDateTime != null
        stories.value = RequestState.Loading
        if (dateIsSelected && zonedDateTime != null) {
            observeGetFilterStories(zonedDateTime)
        } else {
            observeAllStories()
        }
    }

    private fun observeGetFilterStories(zonedDateTime: ZonedDateTime){
       filteredStoriesJob = viewModelScope.launch {
           if(::allStoriesJob.isInitialized){
               allStoriesJob.cancelAndJoin()
           }
            MongoDB.getFilterStories(zonedDateTime = zonedDateTime).collect { result ->
                stories.value = result
            }
        }
    }

    private fun observeAllStories() {
        allStoriesJob = viewModelScope.launch {
            if(::filteredStoriesJob.isInitialized){
                filteredStoriesJob.cancelAndJoin()
            }
            MongoDB.getAllStories().debounce(2000).collect { result ->
                stories.value = result
            }
        }
    }

    fun deleteAllStories(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ){
        if(network == ConnectObserver.Status.Available){
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imageDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imageDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imageToDeleteDao.addImageToDelete(
                                        ImageToDelete(remoteImagePath = imagePath)
                                    )
                                }
                            }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = MongoDB.deleteAllStory()
                        if(result is RequestState.Success){
                            withContext(Dispatchers.Main){
                                onSuccess()
                            }
                        }else if(result is RequestState.Error){
                            withContext(Dispatchers.Main){
                                onError(result.error)
                            }
                        }
                    }
                }
                .addOnFailureListener { onError(it) }
        }else{
            onError(Exception("No internet connection"))
        }
    }
}