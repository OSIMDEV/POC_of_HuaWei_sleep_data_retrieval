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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.osim.health.components.Process
import com.osim.health.model.ObjectBox
import com.osim.health.model.transformed
import com.osim.health.ui.theme.HuaWeiHealthKitTheme
import com.osim.health.viewmodel.HomePageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.osim.health.utils.date2TimeZero
import com.osim.health.utils.formattedDate
import com.osim.health.utils.localDate2Long
import com.osim.health.utils.navTo
import com.osim.health.utils.showToast
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : BaseActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private val vm: HomePageViewModel by viewModels()

    private val huaWeiHealthKitHelper = HuaWeiHealthKitHelper()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HuaWeiHealthKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        Scaffold (
                            topBar = {
                                TopBar()
                            },
                            content = {
                                Box (
                                    modifier = Modifier
                                        .padding(top = it.calculateTopPadding())
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Content()
                                }
                            }
                        )
                        if (vm.showProgress) Process()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar() = TopAppBar(
        title = {
            Text(
                text = getString(R.string.main_page_title),
                fontWeight = FontWeight.Bold,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color.LightGray,
        ),
        actions = {
            Icon(
                Icons.AutoMirrored.Default.ArrowForward,
                null,
                Modifier.clickable(
                    onClick = this@MainActivity::navToDataPage
                )
            )
        }
    )

    @Composable
    fun Content() {
        var which by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formattedDate(getString(R.string.start_time), vm.startDate, separator = " : "),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        which = true
                        showDatePicker = true
                    }
                )
                Text(
                    text = formattedDate(getString(R.string.end_time), vm.endDate, separator = " : "),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        which = false
                        showDatePicker = true
                    }
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
            HuaWeiHealthKitTiles(
                listOf(
                    listOf(
                        ItemInfo(
                            title = "ReqAuth",
                            cb = this@MainActivity::reqAuth,
                        ),
                        ItemInfo(
                            title = "CheckAuth",
                            cb = this@MainActivity::checkAuth,
                        ),
                        ItemInfo(
                            title = "CancelAuth",
                            cb = this@MainActivity::cancelAuth,
                        ),
                    ),
                    listOf(
                        ItemInfo(
                            title = "Get Sleep Records",
                            cb = {
                                getSleepRecord(vm.startDate, vm.endDate)
                            },
                        ),
                    ),
                    listOf(
                        ItemInfo(
                            title = "Nav to Auth Detail Page",
                            cb = this@MainActivity::navToAuthDetailPage,
                        ),
                    ),
                )
            )
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateChange = { date ->
                    if (which) {
                        vm.startDate = date2TimeZero(localDate2Long(date))
                    } else {
                        vm.endDate = date2TimeZero(localDate2Long(date))
                    }
                    showDatePicker = false
                    showToast(this@MainActivity, "${formattedDate("", vm.startDate)} ~ ${formattedDate("", vm.endDate)}")
                },
                title = { Text(text = "Select date") },
            )
        }
    }

    private fun reqAuth() {
        vm.showProgress = true
        huaWeiHealthKitHelper.requestAuth(this@MainActivity) {
            showToast(
                this@MainActivity,
                if (it) "Authorized" else "Unauthorized",
            )
            vm.showProgress = false
        }
    }

    private fun checkAuth() {
        vm.showProgress = true
        huaWeiHealthKitHelper.checkAuth(this@MainActivity) {
            showToast(
                this@MainActivity,
                if (it) "Authorized" else "Unauthorized",
            )
            vm.showProgress = false
        }
    }

    private fun cancelAuth() {
        vm.showProgress = true
        huaWeiHealthKitHelper.cancelAuth(this@MainActivity) {
            showToast(
                this@MainActivity,
                "Unauthorized",
            )
            vm.showProgress = false
        }
    }

    private fun getSleepRecord(startDate: Long, endDate: Long) {
        // 查询科学睡眠详情
        vm.showProgress = true
        val timeout = TimeUnit.MINUTES.toMillis(3).toInt()
        huaWeiHealthKitHelper.getSleepRecord(this@MainActivity, startDate, endDate, timeout) { data, errInfo ->
            data?.apply {
                lifecycleScope.launch(Dispatchers.IO) {
                    ObjectBox.saveSleepRecords(map { it.transformed })
                    navToDataPage()
                }
            } ?: run {
                showToast(this@MainActivity.applicationContext, errInfo)
            }
            vm.showProgress = false
        }
    }

    private fun navToAuthDetailPage() {
        huaWeiHealthKitHelper.navToAuthDetailPage(this@MainActivity)
    }

    private fun navToDataPage() {
        val params = Bundle()
        params.putLong("startDate", date2TimeZero(vm.startDate))
        params.putLong("endDate", date2TimeZero(vm.endDate))
        navTo(this, DataPageActivity::class.java, params)
    }
}

@Composable
fun HuaWeiHealthKitTiles(itemsInfoGroup: List<List<ItemInfo>>) {
    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (itemsInfo in itemsInfoGroup) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(count = itemsInfo.size),
            ) {
                items(itemsInfo) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        propagateMinConstraints = true
                    ) {
                        Card (
                            modifier = Modifier.clickable { it.cb() },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text (
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = it.title,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ItemInfo(val title: String, val cb: ()->Unit)