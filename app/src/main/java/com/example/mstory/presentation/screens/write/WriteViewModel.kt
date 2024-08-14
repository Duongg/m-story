package com.example.mstory.presentation.screens.write

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mstory.data.repository.MongoDB
import com.example.mstory.model.Mood
import com.example.mstory.model.Story
import com.example.mstory.util.Constant
import com.example.mstory.util.RequestState
import com.example.mstory.util.toRealmInstant
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

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
                MongoDB.getSelectedStory(storyId = ObjectId.Companion.from(uiState.selectedStoryId!!))
                    .catch {
                        emit(RequestState.Error(Exception("Story is already deleted")))
                    }
                    .collect { story ->
                        if (story is RequestState.Success) {
                            setSelectedStory(story = story.data)
                            setTitle(title = story.data.title)
                            setDescription(description = story.data.description)
                            setMood(mood = Mood.valueOf(story.data.mood))
                        }
                    }
            }
        }
    }

   private suspend fun updateStory(
       story: Story,
       onError: (String) -> Unit,
       onSuccess: () -> Unit
    ){
       val result = MongoDB.updateStory(story = story.apply {
           _id = ObjectId.Companion.from(uiState.selectedStoryId!!)
           date = if (uiState.updatedDateTime != null) {
               uiState.updatedDateTime!!
           } else {
               uiState.selectedStory!!.date
           }
       })
       if(result is RequestState.Success){
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
                val result = MongoDB.deleteStory(id = ObjectId.from(uiState.selectedStoryId!!))
                if (result is RequestState.Success) {
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