package com.project.mstory

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.project.mstory.navigation.Screen
import com.project.mstory.navigation.SetupNavGraph
import com.project.mstory.ui.theme.MStoryTheme
import com.project.mstory.util.Constant.APP_ID
import com.google.firebase.FirebaseApp
import com.project.mstory.data.database.ImageToDeleteDao
import com.project.mstory.data.database.ImageToUploadDao
import com.project.mstory.data.database.entity.ImageToDelete
import com.project.mstory.util.retryDeletingImageToFirebase
import com.project.mstory.util.retryUploadingImageToFirebase
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
