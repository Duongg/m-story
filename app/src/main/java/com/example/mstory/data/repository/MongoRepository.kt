package com.example.mstory.data.repository

import com.example.mstory.model.Story
import com.example.mstory.util.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Stories = RequestState<Map<LocalDate, List<Story>>>

interface MongoRepository {
    fun configureTheRealm()

    fun getAllStories(): Flow<Stories>
}