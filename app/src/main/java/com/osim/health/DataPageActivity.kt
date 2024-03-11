package com.osim.health

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.osim.health.components.SleepCard
import com.osim.health.components.SleepScoreBoard
import com.osim.health.model.SleepRecord
import com.osim.health.ui.theme.HuaWeiHealthKitTheme
import com.osim.health.viewmodel.DataPageViewModel
import com.osim.health.utils.date2TimeZero
import com.osim.health.utils.dateRange
import com.osim.health.utils.formattedDate
import com.osim.health.utils.localDate2Long
import com.osim.health.utils.showToast
import java.util.concurrent.TimeUnit

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
class DataPageActivity : BaseActivity() {

    companion object {
        const val TAG = "DataPageActivity"
        const val MSG_SHOW_NAV_BAR = 1000
    }

    private val vm: DataPageViewModel by viewModels()

    private lateinit var h: H

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        h = H(vm)
        intent.extras?.getBundle("params")?.apply {
            val now = System.currentTimeMillis()
            vm.startDate = getLong("startDate", now)
            vm.endDate = getLong("endDate", now)
        }
        setContent {
            HuaWeiHealthKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold (
                        topBar = {
                            TopBar()
                        },
                        content = {
                            Box (
                                modifier = Modifier
                                    .padding(top = it.calculateTopPadding())
                                    .fillMaxSize(),
                            ) {
                                Content()
                            }
                        },
                        bottomBar = {
                            if (vm.showSleepScore) {
                                NavigationBar (
                                    containerColor = Color.White
                                ) {
                                    SleepScoreBoard(vm.selectedDate, vm.sleepRecordsForScoring)
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar() = TopAppBar(
        title = {
            Row (
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = getString(R.string.presentation_page_title),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.LightGray,
        ),
        navigationIcon = {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                null,
                Modifier.clickable(
                    onClick = {
                        finish()
                    }
                )
            )
        },
        actions = {
            Icon(
                Icons.TwoTone.Favorite,
                contentDescription = null,
                tint = if (vm.showSleepScore) Color.Red else Color.Gray,
                modifier = Modifier.clickable(
                    onClick = {
                        vm.showDatePicker = !vm.showDatePicker
                        vm.whichUseOfDatePicker = "selected"
                    }
                )
            )
        },
    )

    @Composable
    fun Content() = Column {
        var stateSleepRecords by remember { mutableStateOf(emptyList<SleepRecord>()) }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.padding(8.dp))
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formattedDate(
                            getString(R.string.start_time),
                            vm.startDate,
                            separator = " : "
                        ),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            vm.whichUseOfDatePicker = "start"
                            vm.showDatePicker = true
                        }
                    )
                    Text(
                        text = formattedDate(getString(R.string.end_time), vm.endDate, separator = " : "),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            vm.whichUseOfDatePicker = "end"
                            vm.showDatePicker = true
                        }
                    )
                }
            }
        }
        loadData(startDate = vm.startDate, endDate = vm.endDate) {sleepRecords ->
            stateSleepRecords = sleepRecords.toList()
        }
        if (stateSleepRecords.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(8.dp))
            LazyColumn (
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
            ) {
                itemsIndexed(stateSleepRecords) {index, item ->
                    SleepCard(sleepRecord = item)
                    if (index + 1 != stateSleepRecords.size) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {}
                    }
                }
            }
        }
        if (vm.showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { vm.showDatePicker = false },
                onDateChange = { date ->
                    val curSelectedDate = date2TimeZero(localDate2Long(date))
                    when (vm.whichUseOfDatePicker) {
                        "start" -> {
                            vm.startDate = curSelectedDate
                            vm.showDatePickToast = true
                        }
                        "end" -> {
                            vm.endDate = curSelectedDate
                            vm.showDatePickToast = true
                        }
                        else -> {
                            vm.selectedDate = curSelectedDate
                            val (startDate, endDate) = curSelectedDate.dateRange()
                            loadData(startDate = startDate, endDate = endDate, true) {
                                vm.showSleepScore = true
                                h.removeMessages(MSG_SHOW_NAV_BAR)
                                h.sendEmptyMessageDelayed(MSG_SHOW_NAV_BAR, TimeUnit.SECONDS.toMillis(3L))
                            }
                            vm.showDatePickToast = false
                        }
                    }
                    vm.showDatePicker = false
                    if (vm. showDatePickToast) {
                        showToast(this@DataPageActivity, "${formattedDate("", vm.startDate)} ~ ${formattedDate("", vm.endDate)}")
                    }
                },
                title = { Text(text = "Select date") },
            )
        }
    }

    private fun loadData(startDate: Long, endDate: Long, score: Boolean = false, cb: (List<SleepRecord>)->Unit) {
        vm.getSleepRecords(startDate = startDate, endDate = endDate).observe(
            this@DataPageActivity,
        ) { sleepRecords ->
            val sRecordsVm = if (score) vm.sleepRecordsForScoring else vm.sleepRecords
            sRecordsVm.clear()
            sRecordsVm.addAll(sleepRecords.toList())
            cb(sRecordsVm)
        }
    }

    private class H(private val vm: DataPageViewModel) : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SHOW_NAV_BAR -> vm.showSleepScore = false
            }
        }
    }
}