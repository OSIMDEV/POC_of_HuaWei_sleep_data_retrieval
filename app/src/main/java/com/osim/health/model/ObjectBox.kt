package com.osim.health.model

import android.content.Context
import android.util.Log
import com.osim.health.BuildConfig
import io.objectbox.BoxStore
import io.objectbox.android.Admin
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.exception.DbException
import io.objectbox.exception.FileCorruptException
import io.objectbox.sync.Sync

object ObjectBox {
    private const val TAG = "ObjectBox"

    private lateinit var boxStore: BoxStore

    /**
     * If building the [boxStore] failed, contains the thrown error message.
     */
    var dbExceptionMessage: String? = null
        private set

    fun init(context: Context) {
        // On Android make sure to pass a Context when building the Store.
        boxStore = try {
            MyObjectBox.builder()
                .androidContext(context.applicationContext)
                .build()
        } catch (e: DbException) {
            if (e.javaClass.equals(DbException::class.java) || e is FileCorruptException) {
                // Failed to build BoxStore due to database file issue, store message;
                // checked in NoteListActivity to notify user.
                dbExceptionMessage = e.toString()
                return
            } else {
                // Failed to build BoxStore due to developer error.
                throw e
            }
        }

        if (BuildConfig.DEBUG) {
            val syncAvailable = if (Sync.isAvailable()) "available" else "unavailable"
            Log.d(
                TAG,
                "Using ObjectBox ${BoxStore.getVersion()} (${BoxStore.getVersionNative()}, sync $syncAvailable)"
            )
            // Enable ObjectBox Admin on debug builds.
            // https://docs.objectbox.io/data-browser
            Admin(boxStore).start(context.applicationContext)
        }
    }

    fun saveSleepRecords(data: List<SleepRecord>) {
        boxStore.boxFor(SleepRecord::class.java).put(data)
    }

    fun retrievalSleepRecords(startDate: Long, endDate: Long): ObjectBoxLiveData<SleepRecord> {
        // Prepare a Query for all sleep records, sorted by their start date.
        // The Query is not run until find() is called or
        // it is subscribed to (like ObjectBoxLiveData below does).
        // https://docs.objectbox.io/queries
        val sleepRecordsQuery = boxStore.boxFor(SleepRecord::class.java).query()
            .between(SleepRecord_.startDate, startDate, endDate)
            // Sort notes by most recent first.
            .orderDesc(SleepRecord_.startDate)
            .build()

        // Wrap Query in a LiveData that subscribes to it only when there are active observers.
        // If only used by a single activity or fragment, maybe keep this in their ViewModel.
        return ObjectBoxLiveData(sleepRecordsQuery)
    }
}