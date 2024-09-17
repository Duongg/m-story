package com.project.mstory

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import com.mstory.data.mongo.database.ImageToDeleteDao
import com.mstory.data.mongo.database.ImageToUploadDao
import com.mstory.data.mongo.database.entity.ImageToDelete
import com.mstory.data.mongo.database.entity.ImageToUpload
import com.mstory.data.mongo.model.DataProvider
import com.mstory.ui.theme.MStoryTheme
import com.project.mstory.navigation.SetupNavGraph
import com.project.mstory.presentation.screens.auth.AuthenticationViewModel
import com.project.mstory.util.Constant.APP_ID
import com.project.mstory.util.Screen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class MStoryApplication: Application() {

}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao

    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao

    private var keepSplashOpened = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        FirebaseApp.initializeApp(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MStoryTheme(dynamicColor = false) {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = { keepSplashOpened = false }
                )
            }
        }

        cleanUpCheck(scope = lifecycleScope, imageToUploadDao = imageToUploadDao, imageToDeleteDao = imageToDeleteDao)
    }
}

private fun cleanUpCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
) {
    scope.launch(Dispatchers.IO) {
        val resultUpload = imageToUploadDao.getAllImages()
        resultUpload.forEach { imageToUpload ->
            retryUploadingImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDao.cleanUpImage(imageId = imageToUpload.id)
                    }
                }
            )
        }
        val resultDelete = imageToDeleteDao.getAllImages()
        resultDelete.forEach { imageToDelete ->
            retryDeletingImageToFirebase(
                imageToDelete = imageToDelete,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToDeleteDao.cleanUpImage(imageId = imageToDelete.id)
                    }
                }
            )
        }
    }
}
private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if(user != null && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}

private fun retryUploadingImageToFirebase(
    imageToUpload: ImageToUpload,
    onSuccess: () -> Unit
){
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToUpload.remoteImagePath).putFile(
        imageToUpload.imageUri.toUri(),
        storageMetadata {  },
        imageToUpload.sessionUri.toUri()
    ).addOnSuccessListener { onSuccess() }
}

private fun retryDeletingImageToFirebase(
    imageToDelete: ImageToDelete,
    onSuccess: () -> Unit
){
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToDelete.remoteImagePath).delete()
        .addOnSuccessListener { onSuccess }
}