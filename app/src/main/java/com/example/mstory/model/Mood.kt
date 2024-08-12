package com.example.mstory.model

import androidx.compose.ui.graphics.Color
import com.example.mstory.R
import com.example.mstory.ui.theme.AngryColor
import com.example.mstory.ui.theme.BoredColor
import com.example.mstory.ui.theme.HappyColor
import com.example.mstory.ui.theme.NeutralColor

enum class Mood(
    val icon: Int,
    val contentColor: Color,
    val containerColor: Color
) {
    Neutral(
        icon = R.drawable.neutral,
        contentColor = Color.Black,
        containerColor = NeutralColor,
    ),
    Happy(
        icon = R.drawable.haha,
        contentColor = Color.Black,
        containerColor = HappyColor,
    ),
    Angry(
        icon = R.drawable.angry,
        contentColor = Color.White,
        containerColor = AngryColor,
    ),
    Sad(
        icon = R.drawable.sad,
        contentColor = Color.White,
        containerColor = BoredColor,
    )

}