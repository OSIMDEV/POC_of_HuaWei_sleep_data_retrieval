package com.osim.health

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.osim.health.utils.BetterActivityResult

open class BaseActivity : ComponentActivity() {
    private lateinit var _launcher: BetterActivityResult<Intent, ActivityResult>

    val launcher get() = _launcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _launcher = BetterActivityResult.registerActivityForResult(this)
    }
}