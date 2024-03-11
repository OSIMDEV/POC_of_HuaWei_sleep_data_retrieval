package com.osim.health.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.osim.health.utils.date2TimeZero
import com.osim.health.model.ObjectBox
import com.osim.health.model.SleepRecord

class DataPageViewModel : ViewModel() {
    var startDate by mutableLongStateOf(0)
    var endDate by mutableLongStateOf(0)
    var selectedDate by mutableLongStateOf(0)

    var whichUseOfDatePicker by mutableStateOf("")

    var showDatePicker by mutableStateOf(false)
    var showSleepScore by mutableStateOf(false)

    var showDatePickToast by mutableStateOf(false)

    val sleepRecords = mutableStateListOf<SleepRecord>()
    val sleepRecordsForScoring = mutableStateListOf<SleepRecord>()

    init {
        val now = date2TimeZero(System.currentTimeMillis())
        startDate = now
        endDate = now
        selectedDate = now
    }

    fun getSleepRecords(startDate: Long, endDate: Long) = ObjectBox.retrievalSleepRecords(startDate, endDate)
}