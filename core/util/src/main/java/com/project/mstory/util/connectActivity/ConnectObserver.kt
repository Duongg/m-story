package com.project.mstory.util.connectActivity

import kotlinx.coroutines.flow.Flow


interface ConnectObserver {
    fun observe(): Flow<Status>

    enum class Status {
        Available, UnAvailable, Losing, Lost
    }
}