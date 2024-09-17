package com.mstory.data.mongo.response


import com.google.android.gms.auth.api.signin.GoogleSignInClient

typealias OneTapSignInResponse = Response<GoogleSignInClient>

sealed class Response<out T> {
    object Loading: Response<Nothing>()
    data class Success<out T>(val data: T?): Response<T>()
    data class Failure(val e: Exception): Response<Nothing>()
}