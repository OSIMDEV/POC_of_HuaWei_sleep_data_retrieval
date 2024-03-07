package com.osim.health.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
val dateTimeFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)

@RequiresApi(Build.VERSION_CODES.O)
fun localDate2Long(date: LocalDate) = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun formattedDate(prefix: String, timeInMillis: Long, separator: String = ""): String {
    return "$prefix$separator${timestamp2Date(timeInMillis)}"
}

fun timestamp2Date(timestamp: Long): String {
    val date = Date(timestamp)
    return dateFormat.format(date)
}

fun timestamp2DateTime(timestamp: Long): String {
    val date = Date(timestamp)
    return dateTimeFormat.format(date)
}

fun date2TimeZero(timestamp: Long): Long {
    return dateFormat.parse(timestamp2Date(timestamp))!!.time
}

fun showToast(context: Context, content: String?) = MainScope().launch (Dispatchers.Main) {
    Toast.makeText(
        context,
        content,
        Toast.LENGTH_SHORT
    ).show()
}

inline fun <reified T : Class<out Activity>> navTo(context: Activity, targetClz: T, params: Bundle?) {
    val intent = Intent(context, targetClz)
    params?.apply { intent.putExtra("params", params) }
    context.startActivity(intent)
}