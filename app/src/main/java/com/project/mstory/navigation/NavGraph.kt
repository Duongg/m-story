package com.project.mstory.navigation

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.mstory.data.repository.MongoDB
import com.project.mstory.model.GalleryImage
import com.project.mstory.model.Mood
import com.project.mstory.model.rememberGalleryState
import com.project.mstory.presentation.components.MStoryDialog
import com.project.mstory.presentation.screens.auth.AuthenticationScreen
import com.project.mstory.presentation.screens.auth.AuthenticationViewModel
import com.project.mstory.presentation.screens.home.HomeScreen
import com.project.mstory.presentation.screens.home.HomeViewModel
import com.project.mstory.presentation.screens.write.WriteScreen
import com.project.mstory.presentation.screens.write.WriteViewModel
import com.project.mstory.util.Constant.APP_ID
import com.project.mstory.util.Constant.WRITE_SCREEN_ARGUMENT_KEY
import com.project.mstory.util.RequestState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController, onDataLoaded: () -> Unit) {
    NavHost(startDestination = startDestination, navController = navController) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded,
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuth =  {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            navigateToWriteWithArg = {
                navController.navigate(Screen.Write.passStoryId(storyId = it))
            },
            onDataLoaded = onDataLoaded,
        )
        writeRoute(
            onBackPress = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit){
            onDataLoaded()
        }
        AuthenticationScreen(
            authenticated = authenticated,
            oneTapSignInState = oneTapState,
            loadingState = loadingState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Authenticated Successfully!")
                        viewModel.setLoading(false)
                    },
                    onError = { message ->
                        messageBarState.addError(Exception(message))
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = {message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArg: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
    ) {
    composable(route = Screen.Home.route) {
        val viewModel: HomeViewModel = viewModel()
        val stories by viewModel.stories
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var signOutDialogOpened by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(key1 = stories){
            if(stories !is RequestState.Loading){
                onDataLoaded()
            }
        }
        HomeScreen(
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigateToWrite = navigateToWrite,
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWriteWithArg = navigateToWriteWithArg,
            stories = stories,
        )

        LaunchedEffect(key1 = Unit){
            MongoDB.configureTheRealm()
        }

        MStoryDialog(
            title = "Sign Out",
            message = "Are you sure want to Sign Out ?",
            dialogOpened = signOutDialogOpened,
            onClosed = { signOutDialogOpened = false },
            onConfirm = {
                scope.launch (Dispatchers.IO){
                    val user = App.create(APP_ID).currentUser
                    if(user != null){
                        user.logOut()
                        withContext(Dispatchers.Main){
                            navigateToAuth()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(onBackPress: () -> Unit) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = viewModel()
        val pagerState = rememberPagerState()
        val uiState = viewModel.uiState
        val pageNumber by remember {
            derivedStateOf{pagerState.currentPage}
        }
        val context = LocalContext.current
        val galleryState = rememberGalleryState()
        WriteScreen(
            pagerState = pagerState,
            galleryState = galleryState,
            moodName = {Mood.values()[pageNumber].name},
            onBackPress = onBackPress,
            onDelete = {
                viewModel.deleteStory(
                    onSuccess = {
                        Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                        onBackPress.invoke()
                    },
                    onError = {
                        Toast.makeText(context, "Delete Failed: $it", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onTitleChanged = {viewModel.setTitle(title = it)},
            onDescriptionChanged = {viewModel.setDescription(description = it)},
            uiState = uiState,
            onSaveClicked = {
                viewModel.saveStory(
                    story = it.apply {
                    mood = Mood.values()[pageNumber].name
                    },
                    onSuccess = {onBackPress()},
                    onError = { message ->
                        Toast.makeText(context, "Save Failed: $message", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onUpdatedDateTime = {
                viewModel.updateDateTime(zonedDateTime = it)
            },
            onImageSelect = {
                galleryState.addImage(
                    GalleryImage(
                        image = it,
                        remoteImagePath = ""
                    )
                )
            }
        )
    }
}

