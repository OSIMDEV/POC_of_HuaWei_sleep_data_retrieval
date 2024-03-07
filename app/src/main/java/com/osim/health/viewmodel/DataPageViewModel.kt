package com.osim.health.viewmodel

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.lifecycle.ViewModel
import com.osim.health.utils.date2TimeZero
import com.osim.health.model.ObjectBox

class DataPageViewModel : ViewModel() {
    val startDate: MutableLongState
    val endDate: MutableLongState

    init {
        val now = date2TimeZero(System.currentTimeMillis())
        startDate = mutableLongStateOf(now)
        endDate = mutableLongStateOf(now)
    }

    fun getSleepRecords(startDate: Long, endDate: Long) = ObjectBox.retrievalSleepRecords(startDate, endDate)
}