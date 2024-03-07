package com.osim.health.model

import com.huawei.hihealthkit.data.HiHealthSessionData
import com.huawei.hihealthkit.data.type.HiHealthSessionType.*

val HiHealthSessionData.transformed get() = SleepRecord(
    type = type,
    startDate = startTime,
    endDate = endTime,
    metaData = metaData ?: "",
    deviceModel = sourceDevice.deviceModel ?: "",
    deviceName = sourceDevice.deviceName ?: "",
    deviceType = sourceDevice.deviceType ?: "",
    deviceUniqueCode = sourceDevice.deviceUniqueCode ?: "",
    updateTime = updateTime,
)

val SleepRecord.transformedType get() = when (type) {
    DATA_SESSION_CORE_SLEEP_WAKE -> "Awake"
    DATA_SESSION_CORE_SLEEP_SHALLOW -> "Light Sleep"
    DATA_SESSION_CORE_SLEEP_DREAM -> "REM Sleep"
    DATA_SESSION_CORE_SLEEP_DEEP -> "Deep Sleep"
    DATA_SESSION_CORE_SLEEP_NOON -> "Noon Nap"
    DATA_SESSION_CORE_SLEEP_BED -> "In Bed"
    else -> "Ignored"
}