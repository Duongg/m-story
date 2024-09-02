package com.project.mstory.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mstory.data.mongo.repository.Stories
import com.project.mstory.R
import com.project.mstory.util.RequestState
import java.time.ZonedDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    navigateToWrite: () -> Unit,
    onSignOutClicked: () -> Unit,
    onDeleteAllStoriesClicked: () -> Unit,
    navigateToWriteWithArg: (String) -> Unit,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit,
    stories: Stories,
) {
    var padding by remember { mutableStateOf(PaddingValues()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllStoriesClicked = onDeleteAllStoriesClicked,
    ) {
       Scaffold(
           modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
           topBar = {
               HomeTopBar(
                   scrollBehavior = scrollBehavior,
                   onMenuClicked = onMenuClicked,
                   dateIsSelected = dateIsSelected,
                   onDateSelected = onDateSelected,
                   onDateReset = onDateReset,
               )
           },
           floatingActionButton = {
               FloatingActionButton(
                   modifier = Modifier.padding(end = padding.calculateEndPadding(LayoutDirection.Ltr)),
                   onClick = navigateToWrite
               ) {
                   Icon(imageVector = Icons.Default.Edit, contentDescription ="New story" )
               }
           },
           content = {
               padding = it
               when(stories){
                   is RequestState.Success -> {
                       HomeContent(
                           paddingValues = it,
                           stories = stories.data,
                           onClick = navigateToWriteWithArg,
                       )
                   }

                   is RequestState.Error -> {
                       EmptyScreen(title = "Error", subtitle = "${stories.error.message}")
                   }

                   is RequestState.Loading -> {
                       Box(
                           modifier = Modifier.fillMaxSize(),
                           contentAlignment = Alignment.Center,
                       ) {
                           CircularProgressIndicator()
                       }
                   }

                   else -> {}
               }
           }
       )
   }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllStoriesClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
           ModalDrawerSheet(
               content = {
                   Box(
                       modifier = Modifier
                           .fillMaxWidth()
                           .height(250.dp),
                       contentAlignment = Alignment.Center
                   ) {
                       Image(modifier = Modifier.size(200.dp), painter = painterResource(id = R.drawable.logo_1), contentDescription = "Logo image")
                   }
                   NavigationDrawerItem(
                       label = {
                           Row (modifier = Modifier.padding(horizontal = 16.dp)){
                               Icon(painterResource(id = R.drawable.google_logo), contentDescription = "", tint = MaterialTheme.colorScheme.surface)
                               Spacer(modifier = Modifier.width(12.dp))
                               Text(text = "Sign Out")
                           }
                       },
                       selected = false,
                       onClick = onSignOutClicked
                   )
                   NavigationDrawerItem(
                       label = {
                           Row (modifier = Modifier.padding(horizontal = 16.dp)){
                               Icon(imageVector = Icons.Default.Delete, contentDescription = "", tint = MaterialTheme.colorScheme.surface)
                               Spacer(modifier = Modifier.width(12.dp))
                               Text(text = "Delete All Stories")
                           }
                       },
                       selected = false,
                       onClick = onDeleteAllStoriesClicked
                   )
               }
           )
        },
        content = content,
        )
}