package com.osim.health.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.KeyboardArrowDown
import androidx.compose.material.icons.twotone.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.osim.health.model.SleepRecord
import com.osim.health.model.transformedType
import com.osim.health.utils.timestamp2DateTime

@Composable
fun SleepCard(sleepRecord: SleepRecord) {
    val expanded = remember { mutableStateOf(false) }
    Card (
        modifier = Modifier
            .clickable { expanded.value = !expanded.value }
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = sleepRecord.transformedType, fontSize = 12.sp)
                Text(text = timestamp2DateTime(sleepRecord.startDate), fontSize = 12.sp)
                Text(text = timestamp2DateTime(sleepRecord.endDate), fontSize = 12.sp)
                val icons = if (expanded.value) {
                    Icons.TwoTone.KeyboardArrowUp
                } else {
                    Icons.TwoTone.KeyboardArrowDown
                }
                Icon(
                    imageVector = icons,
                    tint = Color.Gray,
                    contentDescription = null
                )
            }
            if (expanded.value) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.Gray
                )
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "metaData : ${sleepRecord.metaData}", fontSize = 12.sp)
                    Text(text = "deviceModel : ${sleepRecord.deviceModel}", fontSize = 12.sp)
                    Text(text = "deviceName : ${sleepRecord.deviceName}", fontSize = 12.sp)
                    Text(text = "deviceType : ${sleepRecord.deviceType}", fontSize = 12.sp)
                    Text(text = "deviceUniqueCode : ${sleepRecord.deviceUniqueCode}", fontSize = 12.sp)
                    Text(text = "updateTime : ${timestamp2DateTime(sleepRecord.updateTime)}", fontSize = 12.sp)
                }
            }
        }
    }
}