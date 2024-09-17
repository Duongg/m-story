package com.mstory.data.mongo.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.mstory.data.mongo.response.OneTapSignInResponse
import com.mstory.data.mongo.response.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val googleSignInOptions: GoogleSignInOptions,
): AuthRepository {
    override suspend fun onTapSignIn(context: Context): OneTapSignInResponse {
        return try {
            val googleAuthProvider = GoogleSignIn.getClient(context, googleSignInOptions)
            Response.Success(googleAuthProvider)
        }catch (e: Exception){
            return try {
                val googleAuthProvider = GoogleSignIn.getClient(context, googleSignInOptions)
                Response.Success(googleAuthProvider)
            }catch (e: Exception){
                Response.Failure(e)
            }
        }
    }
}