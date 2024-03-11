package com.osim.health.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.osim.health.utils.date2TimeZero

class HomePageViewModel : ViewModel() {
    var startDate by mutableLongStateOf(0)
    var endDate by mutableLongStateOf(0)
    var showProgress by mutableStateOf(false)

    init {
        val now = date2TimeZero(System.currentTimeMillis())
        startDate = now
        endDate = now
    }
}