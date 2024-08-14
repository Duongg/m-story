package com.example.mstory.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.mstory.model.Story
import com.example.mstory.presentation.components.MStoryDialog
import com.example.mstory.util.toInstant
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopBar(
    selectedStory: Story?,
    moodName: () -> String,
    onBackPress: () -> Unit,
    onDelete: () -> Unit,
    onUpdatedDateTime: (ZonedDateTime) -> Unit,
){
    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val formattedDate = remember(key1 = currentDate) {
        DateTimeFormatter.ofPattern("dd MMM yyyy").format(currentDate).uppercase()
    }
    val formattedTime = remember(key1 = currentTime) {
        DateTimeFormatter.ofPattern("hh:mm a").format(currentTime).uppercase()
    }
    val selectedStoryDateTime = remember(selectedStory) {
        if(selectedStory != null){
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date.from(selectedStory.date.toInstant())).uppercase()
        }else{
            "Unknown"
        }
    }
    var dateTimeUpdated by remember {
        mutableStateOf(false)
    }
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
            }
        },
        title = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = moodName(),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if(selectedStory != null && dateTimeUpdated) "$formattedDate, $formattedTime"
                    else if(selectedStory != null) selectedStoryDateTime
                    else "$formattedDate, $formattedTime",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            if(dateTimeUpdated){
                IconButton(onClick = {
                    currentDate = LocalDate.now()
                    currentTime = LocalTime.now()
                    dateTimeUpdated = false
                    onUpdatedDateTime(ZonedDateTime.of(currentDate, currentTime, ZoneId.systemDefault()))
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "", tint = MaterialTheme.colorScheme.onSurface)
                }
            }else{
                IconButton(onClick = {
                    dateDialog.show()
                }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            if(selectedStory != null){
                DeleteStoryAction(
                    selectedStory = selectedStory,
                    onDelete = onDelete,
                )
            }
        }
    )

    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date {localDate ->
            currentDate = localDate
            timeDialog.show()
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )
    ClockDialog(
        state = timeDialog,
        selection = ClockSelection.HoursMinutes{ hours, minutes ->
            currentTime = LocalTime.of(hours, minutes)
            dateTimeUpdated = true
            onUpdatedDateTime(
                ZonedDateTime.of(currentDate, currentTime, ZoneId.systemDefault())
            )
        }
    )
}

@Composable
fun DeleteStoryAction(
    selectedStory: Story?,
    onDelete: () -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var openDialog by remember {
        mutableStateOf(false)
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(text = "Delete") },
            onClick = {
                openDialog = true
                expanded = false
            }
        )
    }
    MStoryDialog(
        title = "Delete",
        message = "Do you want to delete this story: ${selectedStory?.title} ?",
        dialogOpened = openDialog,
        onClosed = { openDialog = false },
        onConfirm = onDelete,
    )
    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

}