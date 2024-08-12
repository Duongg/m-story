package com.example.mstory.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mstory.data.repository.MongoDB
import com.example.mstory.data.repository.Stories
import com.example.mstory.util.RequestState
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var stories: MutableState<Stories> = mutableStateOf(RequestState.Idle)

    init {
        observeAllStories()
    }

    private fun observeAllStories() {
        viewModelScope.launch {
            MongoDB.getAllStories().collect { result ->
                stories.value = result
            }
        }
    }
}