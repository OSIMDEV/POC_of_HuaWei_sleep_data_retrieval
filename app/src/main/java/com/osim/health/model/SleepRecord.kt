package com.osim.health.model

import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class SleepRecord (
    @Id
    var id: Long = 0L,

    val type: Int,

    @Unique(onConflict = ConflictStrategy.REPLACE)
    @Index
    val startDate: Long,

    val endDate: Long,

    /// extra information
    val metaData: String,

    val deviceModel: String,

    val deviceName: String,

    val deviceType: String,

    val deviceUniqueCode: String,

    val updateTime: Long,
)