package com.osim.health.viewmodel

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.osim.health.utils.date2TimeZero

class HomePageViewModel : ViewModel() {
    val startDate: MutableLongState
    val endDate: MutableLongState
    val showProgress = mutableStateOf(false)

    init {
        val now = date2TimeZero(System.currentTimeMillis())
        startDate = mutableLongStateOf(now)
        endDate = mutableLongStateOf(now)
    }
}