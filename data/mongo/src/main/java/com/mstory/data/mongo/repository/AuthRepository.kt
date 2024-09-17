package com.mstory.data.mongo.repository

import android.content.Context
import com.mstory.data.mongo.response.OneTapSignInResponse

interface AuthRepository {
    suspend fun onTapSignIn(context: Context): OneTapSignInResponse
}