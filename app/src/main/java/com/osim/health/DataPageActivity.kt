package com.osim.health

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.osim.health.model.SleepRecord
import com.osim.health.ui.theme.HuaWeiHealthKitTheme
import com.osim.health.viewmodel.DataPageViewModel
import com.osim.health.utils.date2TimeZero
import com.osim.health.utils.formattedDate
import com.osim.health.utils.localDate2Long
import com.osim.health.utils.showToast

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
class DataPageActivity : BaseActivity() {

    companion object {
        const val TAG = "DataPageActivity"
    }

    private val vm: DataPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.getBundle("params")?.apply {
            val now = System.currentTimeMillis()
            vm.startDate.longValue = getLong("startDate", now)
            vm.endDate.longValue = getLong("endDate", now)
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
                        }
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
        }
    )

    @Composable
    fun Content() = Column {
        var startDate by remember { vm.startDate }
        var endDate by remember { vm.endDate }
        var which by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        val stateSleepRecords = remember { mutableStateOf(emptyList<SleepRecord>()) }
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
                            startDate,
                            separator = " : "
                        ),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            which = true
                            showDatePicker = true
                        }
                    )
                    Text(
                        text = formattedDate(getString(R.string.end_time), endDate, separator = " : "),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            which = false
                            showDatePicker = true
                        }
                    )
                }
            }
        }
        loadData(startDate = startDate, endDate = endDate) {sleepRecords ->
            stateSleepRecords.value = sleepRecords.toList()
        }
        if (stateSleepRecords.value.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(8.dp))
            LazyColumn (
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
            ) {
                val sleepRecords = stateSleepRecords.value
                itemsIndexed(sleepRecords) {index, item ->
                    SleepCard(sleepRecord = item)
                    if (index + 1 != sleepRecords.size) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {}
                    }
                }
            }
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateChange = { date ->
                    if (which) {
                        startDate = date2TimeZero(localDate2Long(date))
                    } else {
                        endDate = date2TimeZero(localDate2Long(date))
                    }
                    showDatePicker = false
                    showToast(this@DataPageActivity, "${formattedDate("", startDate)} ~ ${formattedDate("", endDate)}")
                },
                title = { Text(text = "Select date") },
            )
        }
    }

    private fun loadData(startDate: Long, endDate: Long, cb: (List<SleepRecord>)->Unit) {
        vm.getSleepRecords(startDate = startDate, endDate = endDate).observe(
            this@DataPageActivity,
        ) { sleepRecords ->
           cb(sleepRecords.toList())
        }
    }
}