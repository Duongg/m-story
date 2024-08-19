package com.project.mstory.presentation.screens.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.project.mstory.model.GalleryState
import com.project.mstory.model.Mood
import com.project.mstory.model.Story
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import java.time.ZonedDateTime


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WriteScreen(
    pagerState: PagerState,
    galleryState: GalleryState,
    moodName: () -> String,
    onBackPress: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onSaveClicked: (Story) -> Unit,
    onUpdatedDateTime: (ZonedDateTime) -> Unit,
    uiState: UiState,
    onImageSelect: (Uri) -> Unit,
    ) {
    LaunchedEffect(key1 = uiState.mood){
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedStory = uiState.selectedStory,
                moodName = moodName,
                onBackPress = onBackPress,
                onDelete = onDelete,
                onUpdatedDateTime = onUpdatedDateTime,
            )
        },
        content = {
            WriteContent(
                uiState = uiState,
                pagerState = pagerState,
                galleryState = galleryState,
                title = uiState.title,
                description = uiState.description,
                onTitleChanged = onTitleChanged,
                onDescriptionChanged = onDescriptionChanged,
                paddingValues = it,
                onSaveClicked = onSaveClicked,
                onImageSelect = onImageSelect,
            )
        }
    )
}