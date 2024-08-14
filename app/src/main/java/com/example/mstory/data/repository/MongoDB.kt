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
import io.realm.kotlin.types.ObjectId
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

    override fun getSelectedStory(storyId: ObjectId): Flow<RequestState<Story>> {
        return if(user != null){
            try {
                realm.query<Story>(query = "_id == $0", storyId).asFlow().map {
                    RequestState.Success(data = it.list.first())
                }
            }catch (e: Exception){
                flow{ emit(RequestState.Error(e)) }
            }
        }else{
           flow{ emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertNewStory(story: Story): RequestState<Story> {
        return if(user != null){
            realm.write {
                try {
                    val addedStory = copyToRealm(story.apply { ownerId = user.identity })
                    RequestState.Success(data = addedStory)
                }catch (e: Exception){
                    RequestState.Error(e)
                }
            }
        }else{
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateStory(story: Story): RequestState<Story> {
        return if(user != null){
            realm.write {
                try {
                   val queriedStory = query<Story>(query = "_id == $0", story._id).first().find()
                    if(queriedStory != null){
                        queriedStory.title = story.title
                        queriedStory.description = story.description
                        queriedStory.mood = story.mood
                        queriedStory.images = story.images
                        queriedStory.date = story.date
                        RequestState.Success(data = queriedStory)
                    }else{
                        RequestState.Error(Exception("Queried Story does not exist"))
                    }
                }catch (e: Exception){
                    RequestState.Error(e)
                }
            }
        }else{
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteStory(id: ObjectId): RequestState<Story> {
        return if(user != null){
            realm.write {
                val story = query<Story>(
                    query = "_id == $0 AND ownerId == $1",
                    id,
                    user.identity
                ).first().find()
                if (story != null) {
                    try {
                        delete(story)
                        RequestState.Success(data = story)
                    } catch (e: Exception){
                        RequestState.Error(e)
                    }
                }else{
                    RequestState.Error(Exception("Story does not exist"))
                }
            }
        }else{
            RequestState.Error(UserNotAuthenticatedException())
        }
    }
}

private class UserNotAuthenticatedException: Exception("User is not logged in")