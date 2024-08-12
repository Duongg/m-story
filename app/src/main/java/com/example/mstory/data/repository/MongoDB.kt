package com.example.mstory.data.repository

import com.example.mstory.model.Story
import com.example.mstory.util.Constant.APP_ID
import com.example.mstory.util.RequestState
import com.example.mstory.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

object MongoDB: MongoRepository {
    private val app = App.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm
    init {
        configureTheRealm()
    }
    override fun configureTheRealm() {
        if(user != null){
            val config = SyncConfiguration.Builder(user, setOf(Story::class))
                .initialSubscriptions{ sub ->
                    add(
                        query = sub.query<Story>("ownerId == $0", user.identity),
                        name = "User Stories"
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllStories(): Flow<Stories> {
        return if(user != null){
            try {
                realm.query<Story>("ownerId == $0", user.identity)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map {result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )

                    }
            }catch (e: Exception){
                flow {
                    emit(RequestState.Error(UserNotAuthenticatedException()))
                }
            }
        }else{
            flow {
                emit(RequestState.Error(UserNotAuthenticatedException()))
            }
        }
    }
}

private class UserNotAuthenticatedException: Exception("User is not logged in")