package com.mstory.data.mongo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mstory.data.mongo.response.OneTapSignInResponse
import com.mstory.data.mongo.response.Response


object DataProvider {

    var oneTapSignInResponse by mutableStateOf<OneTapSignInResponse>(Response.Success(null))


}