package com.project.mstory.presentation.screens.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.mstory.model.GalleryState
import com.project.mstory.model.Mood
import com.project.mstory.model.Story
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.project.mstory.model.GalleryImage
import java.time.ZonedDateTime
import kotlin.math.max


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
    onImageDeleteClick: (GalleryImage) -> Unit,
) {
    var selectedGalleryImage by remember {
        mutableStateOf<GalleryImage?>(null)
    }
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
        content = { paddingValues ->
            WriteContent(
                uiState = uiState,
                pagerState = pagerState,
                galleryState = galleryState,
                title = uiState.title,
                description = uiState.description,
                onTitleChanged = onTitleChanged,
                onDescriptionChanged = onDescriptionChanged,
                paddingValues = paddingValues,
                onSaveClicked = onSaveClicked,
                onImageSelect = onImageSelect,
                onImageClicked = {selectedGalleryImage = it}
            )
            AnimatedVisibility(visible = selectedGalleryImage != null) {
                Dialog(onDismissRequest = {selectedGalleryImage = null}) {
                    if(selectedGalleryImage != null){
                        ZoomableImage(
                            selectedGalleryImage = selectedGalleryImage!!,
                            onCloseClicked = { selectedGalleryImage = null },
                            onDeleteClicked = {
                                if(selectedGalleryImage != null){
                                    onImageDeleteClick(selectedGalleryImage!!)
                                    selectedGalleryImage = null
                                }
                            }
                        )
                    }

                }
            }
        }
    )
}

@Composable
fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    var offSetX by remember {
        mutableStateOf(0f)
    }
    var offSetY by remember {
        mutableStateOf(0f)
    }
    var scale by remember {
        mutableStateOf(1f)
    }

    Box(
        modifier = Modifier.pointerInput(Unit){
            detectTransformGestures { centroid, pan, zoom, rotation ->
                scale = maxOf(1f, minOf(scale * zoom, 5f))
                val maxX = (size.width * (scale - 1)) /2
                val minX = -maxX
                offSetX = maxOf(minX, minOf(maxX, offSetX + pan.x))
                val maxY = (size.height * (scale - 1)) /2
                val minY = -maxY
                offSetY = maxOf(minY, minOf(maxY, offSetY + pan.y))
            }
        }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offSetX,
                    translationY = offSetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = ""
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onCloseClicked) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "")
                Text(text = "Close")
            }
            Button(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "")
                Text(text = "Delete")
            }
        }
    }

}