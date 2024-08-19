package com.project.mstory.presentation.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.project.mstory.model.Mood
import com.project.mstory.model.Story
import com.project.mstory.ui.theme.Elevation
import com.project.mstory.util.fetchImagesFromFirebase
import com.project.mstory.util.toInstant
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@Composable
fun StoryHeader(moodName: String, time: Instant) {
    val mood by remember { mutableStateOf(Mood.valueOf(moodName)) }
    Row (modifier = Modifier
        .fillMaxWidth()
        .background(mood.containerColor)
        .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ){
        Row (verticalAlignment = Alignment.CenterVertically){
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = mood.icon),
                contentDescription = "",
                )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            )
        }
        Text(
            text = SimpleDateFormat("hh:mm a", Locale.US).format(Date.from(time)),
            color = mood.contentColor,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )
    }
}
@Composable
fun StoryHolder(story: Story, onClick: (String) -> Unit) {
    val context = LocalContext.current
    var composeHeight by remember {
        mutableStateOf(0.dp)
    }
    val localDensity = LocalDensity.current
    var galleryOpened by remember {
        mutableStateOf(false)
    }
    var galleryLoading by remember {
        mutableStateOf(false)
    }
    val downloadedImages = remember {
        mutableStateListOf<Uri>()
    }

    LaunchedEffect(key1 = galleryOpened) {
        if (galleryOpened && downloadedImages.isEmpty()) {
            galleryLoading = true
            fetchImagesFromFirebase(
                images = story.images,
                onImageDownload = { image ->
                    downloadedImages.add(image)
                },
                onImageDownloadFailed = {
                    Toast.makeText(
                        context,
                        "Image not uploaded yet. Wait to download",
                        Toast.LENGTH_SHORT
                    ).show()
                    galleryLoading = false
                    galleryOpened = false
                },
                onReadyToDisplay = {
                    galleryLoading = false
                    galleryOpened = true
                }
            )
        }
    }

    Row(modifier = Modifier
        .clickable(indication = null,
            interactionSource = remember {
                MutableInteractionSource()
            }
        ) { onClick(story._id.toString()) }
    ) {
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            modifier = Modifier
                .width(2.dp)
                .height(composeHeight + 14.dp),
            tonalElevation = Elevation.Level1
        ){}
        Spacer(modifier = Modifier.width(20.dp))
        Surface(
            modifier = Modifier
                .clip(shape = Shapes().medium)
                .onGloballyPositioned {
                    composeHeight = with(localDensity) { it.size.height.toDp() }
                },
            tonalElevation = Elevation.Level1
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                StoryHeader(moodName = story.mood, time = story.date.toInstant())
                Text(
                    modifier = Modifier.padding(all = 14.dp),
                    text = story.description,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if(story.images.isNotEmpty()){
                    ShowGalleryButton(
                        galleryOpened = galleryOpened,
                        galleryLoading = galleryLoading,
                        onClick = {
                            galleryOpened = !galleryOpened
                        }
                    )
                }
                AnimatedVisibility(
                    visible = galleryOpened && !galleryLoading,
                    enter = fadeIn() + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(all = 14.dp)) {
                        Gallery(images = downloadedImages)
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(localDate: LocalDate) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically){
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%02d", localDate.dayOfMonth),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = "${localDate.dayOfWeek}".take(3),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = localDate.month.toString().lowercase().replaceFirstChar { it.titlecase() },
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
            Text(
                text = "${localDate.year}",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}

@Composable
fun ShowGalleryButton(
    galleryOpened: Boolean,
    galleryLoading: Boolean,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = if (galleryOpened) {
                if(galleryLoading) "Loading..." else "Hide Gallery"
            }else{
                "Show Gallery"
            },
            style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)
        )
    }

}