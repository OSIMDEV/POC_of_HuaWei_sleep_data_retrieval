package com.osim.health.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huawei.hihealthkit.data.type.HiHealthSessionType.DATA_SESSION_CORE_SLEEP_DEEP
import com.huawei.hihealthkit.data.type.HiHealthSessionType.DATA_SESSION_CORE_SLEEP_DREAM
import com.huawei.hihealthkit.data.type.HiHealthSessionType.DATA_SESSION_CORE_SLEEP_SHALLOW
import com.huawei.hihealthkit.data.type.HiHealthSessionType.DATA_SESSION_CORE_SLEEP_WAKE
import com.osim.health.R
import com.osim.health.algorithms.score
import com.osim.health.model.SleepRecord
import com.osim.health.utils.dateRange
import com.osim.health.utils.toDateStr
import kotlin.math.roundToInt

const val TAG = "SleepScoreBoard"

@Composable
fun SleepScoreBoard(selectedDate: Long, sleepRecords: SnapshotStateList<SleepRecord>) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row (
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = selectedDate.toDateStr(pattern = "yyyy-MM-dd"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W400
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = LocalContext.current.getString(R.string.score_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W400
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "${aggregateData(selectedDate = selectedDate, sleepRecords = sleepRecords)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W700,
                        color = Color.Red,
                    )
                }
            }
        }
    }
}

private fun aggregateData(age: Double = 39.0, selectedDate: Long, sleepRecords: List<SleepRecord>): Int {
    val dataset = mutableListOf<SleepRecord>()
    sleepRecords.forEach {
        val (startDate, endDate) = selectedDate.dateRange()
        if (it.startDate in startDate .. endDate) {
            dataset.add(it)
        }
    }
    return Statistics(dataset).calculate(age)
}

private data class Statistics(private val dataset: List<SleepRecord>) {

    fun calculate(age: Double): Int {
        val (total, deep, rem) = components
        return score(total, deep, rem, age).roundToInt()
    }

    private val components: List<Double> get() = arrayOf(
        dataset.filter {
            when (it.type) {
                DATA_SESSION_CORE_SLEEP_WAKE,
                DATA_SESSION_CORE_SLEEP_SHALLOW,
                DATA_SESSION_CORE_SLEEP_DREAM,
                DATA_SESSION_CORE_SLEEP_DEEP -> true
                else -> false
            }
        },
        dataset.filter { it.type == DATA_SESSION_CORE_SLEEP_DEEP },
        dataset.filter { it.type == DATA_SESSION_CORE_SLEEP_DREAM },
    ).map { it.size / 60.0 }
}