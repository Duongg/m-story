package com.example.mstory.data.repository

import com.example.mstory.model.Story
import com.example.mstory.util.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Stories = RequestState<Map<LocalDate, List<Story>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllStories(): Flow<Stories>
    fun getSelectedStory(storyId: ObjectId): Flow<RequestState<Story>>
    suspend fun insertNewStory(story: Story): RequestState<Story>
    suspend fun updateStory(story: Story): RequestState<Story>
    suspend fun deleteStory(id: ObjectId): RequestState<Story>
}