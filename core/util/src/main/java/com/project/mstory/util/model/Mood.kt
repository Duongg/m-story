package com.project.mstory.util.model

import androidx.compose.ui.graphics.Color
import com.mstory.ui.theme.AngryColor
import com.mstory.ui.theme.BoredColor
import com.mstory.ui.theme.HappyColor
import com.mstory.ui.theme.NeutralColor
import com.project.mstory.util.R

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