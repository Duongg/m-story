package com.project.mstory.presentation.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.mstory.model.Story
import com.project.mstory.presentation.components.DateHeader
import com.project.mstory.presentation.components.StoryHolder
import java.time.LocalDate


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    stories: Map<LocalDate, List<Story>>,
    onClick: (String) -> Unit
) {
    if (stories.isNotEmpty()) {
        LazyColumn(modifier = Modifier
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .padding(top = paddingValues.calculateTopPadding())
        ) {
            stories.forEach { (localDate, stories) ->
                stickyHeader(key = localDate) {
                    DateHeader(localDate = localDate)
                }

                items(items = stories, key = { it._id.toString() }) {
                    StoryHolder(story = it, onClick = onClick)
                }
            }
        }
    } else {
        EmptyScreen()
    }
}

@Composable
fun EmptyScreen(
    title: String = "Empty Story",
    subtitle: String = "Write Something"
) {
  Column(
      modifier = Modifier
          .fillMaxSize()
          .padding(all = 24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
  ) {
      Text(
          text = title,
          style = TextStyle(
              fontSize = MaterialTheme.typography.titleMedium.fontSize,
              fontWeight = FontWeight.Medium
          )
      )
      Text(
          text = subtitle,
          style = TextStyle(
              fontSize = MaterialTheme.typography.bodySmall.fontSize,
              fontWeight = FontWeight.Normal
          )
      )
  }
}