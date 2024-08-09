package com.example.mstory.navigation

import com.example.mstory.util.Constant.WRITE_SCREEN_ARGUMENT_KEY

sealed class Screen(val route: String) {
    object Authentication : Screen(route = "authentication_screen")
    object Home : Screen(route = "home_screen")
    object Write :
        Screen(route = "write_screen?$WRITE_SCREEN_ARGUMENT_KEY={$WRITE_SCREEN_ARGUMENT_KEY}") {
        fun passStory(storyId: String) = "write_screen?$WRITE_SCREEN_ARGUMENT_KEY=$storyId"
    }
}