package com.mstory.data.mongo.repository

import com.project.mstory.util.model.Story
import com.project.mstory.util.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate
import java.time.ZonedDateTime

typealias Stories = RequestState<Map<LocalDate, List<Story>>>

interface MongoRepository {
    fun configureTheRealm()
    fun getAllStories(): Flow<Stories>
    fun getFilterStories(zonedDateTime: ZonedDateTime): Flow<Stories>
    fun getSelectedStory(storyId: ObjectId): Flow<RequestState<Story>>
    suspend fun insertNewStory(story: Story): RequestState<Story>
    suspend fun updateStory(story: Story): RequestState<Story>
    suspend fun deleteStory(id: ObjectId): RequestState<Story>
    suspend fun deleteAllStory(): RequestState<Boolean>
}