package com.project.mstory.presentation.screens.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mstory.data.mongo.model.DataProvider
import com.mstory.data.mongo.response.Response
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    authViewModel: AuthenticationViewModel,
    authenticated: Boolean,
    loadingState: Boolean,
    messageBarState: MessageBarState,
    navigateToHome: () -> Unit,
) {
    var context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credentials = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                var account = credentials.getResult(ApiException::class.java)
                val token = authViewModel.handleSignInWithGoogle(account)
                val credential = GoogleAuthProvider.getCredential(token, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        authViewModel.setLoading(true)
                        if (task.isSuccessful) {
                            authViewModel.signInWithMongoAtlas(
                                token,
                                onSuccess = { authViewModel.setLoading(false) },
                                onError = { authViewModel.setLoading(false) })
                        } else {
                            task.exception?.let { it -> Exception(it) }
                        }
                    }
            }
            catch (e: ApiException) {
                Log.e("LoginScreen:Launcher","Login One-tap $e")
            }
        }
        else if (result.resultCode == Activity.RESULT_CANCELED){
            Log.e("LoginScreen:Launcher","OneTapClient Canceled")
        }
    }

    fun launch(googleSignInClient: GoogleSignInClient) {
        launcher.launch(googleSignInClient.signInIntent)
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = {
            ContentWithMessageBar(messageBarState = messageBarState) {
                AuthenticationContent(
                    loadingState = loadingState,
                    onButtonClicked =  {
                        authViewModel.handleButtonClicked(context)
                    },

                )
            }
        }
    )

    when(val oneTapSignInResponse = DataProvider.oneTapSignInResponse) {
        is Response.Loading ->  {
            Log.i("Login:OneTap", "Loading")
            AuthLoginProgressIndicator()
        }
        is Response.Success -> oneTapSignInResponse.data?.let { signInResult ->
            LaunchedEffect(signInResult) {
                launch(signInResult)
                Log.i("Login:OneTap", "signInResult.credential")
            }
        }
        is Response.Failure -> LaunchedEffect(Unit) {
            Log.e("Login:OneTap", "${oneTapSignInResponse.e}")
        }
    }


    LaunchedEffect(key1 = authenticated) {
        if (authenticated) {
            navigateToHome()
        }
    }
}

@Composable
fun AuthLoginProgressIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.tertiary,
            strokeWidth = 5.dp
        )
    }
}