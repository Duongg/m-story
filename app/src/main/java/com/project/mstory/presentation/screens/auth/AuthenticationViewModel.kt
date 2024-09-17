package com.project.mstory.presentation.screens.auth

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.mstory.data.mongo.model.DataProvider
import com.mstory.data.mongo.repository.AuthRepository
import com.mstory.data.mongo.response.OneTapSignInResponse
import com.mstory.data.mongo.response.Response
import com.project.mstory.util.Constant.APP_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val repository: AuthRepository,
): ViewModel() {

    var authenticated = mutableStateOf(false)
        private set

    var loadingState = mutableStateOf(false)
        private set

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }


    fun handleSignInWithGoogle(account: GoogleSignInAccount?): String {
        return  account?.idToken.toString()
    }

    fun handleButtonClicked(context: Context) = CoroutineScope(Dispatchers.IO).launch{
        DataProvider.oneTapSignInResponse = Response.Loading
        DataProvider.oneTapSignInResponse = repository.onTapSignIn(context)
    }


    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO){
                    App.create(APP_ID).login(
                        Credentials.jwt(tokenId)
                    ).loggedIn
                }

                withContext(Dispatchers.Main){
                    if(result){
                        onSuccess()
                        delay(600)
                        authenticated.value = true
                    }else{
                        onError(java.lang.Exception("User is not login"))
                    }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                   onError(e)
                }
            }
        }
    }
}