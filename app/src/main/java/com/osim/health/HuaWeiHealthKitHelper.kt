package com.osim.health

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.huawei.hihealth.error.HiHealthError
import com.huawei.hihealthkit.HiHealthDataQueryOption
import com.huawei.hihealthkit.HiHealthDataQuery
import com.huawei.hihealthkit.data.HiHealthSessionData
import com.huawei.hihealthkit.data.store.HiHealthDataStore
import com.huawei.hihealthkit.data.type.HiHealthSessionType
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.Scopes
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class HuaWeiHealthKitHelper {
    companion object {
        private const val TAG = "HuaWeiHealthKitHelper"
        private const val DEBUG = false

        private const val APP_ID_KEY = "com.huawei.hms.client.appid"
        private const val URI_THIRD_PARTY_ACCOUNT_AUTH = "huaweischeme://healthapp/oauth/thirdPartyAccountAuth?app_id="

        private val scopes get() = arrayOf(
            Scopes.HEALTHKIT_SLEEP_READ,
            Scopes.HEALTHKIT_HISTORYDATA_OPEN_WEEK,
            Scopes.HEALTHKIT_HISTORYDATA_OPEN_MONTH,
            Scopes.HEALTHKIT_HISTORYDATA_OPEN_YEAR,
        )

        fun sha256(context: Context): String? {
            try {
                val pkgInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                val signatures = pkgInfo.signatures
                val cert = signatures[0].toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                md.update(cert)
                val fingerprint = md.digest()
                return fingerprint.toHexString()
            } catch (ex: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Package name not found", ex)
            } catch (ex: NoSuchAlgorithmException) {
                Log.e(TAG, "SHA-256 algorithm not found", ex)
            }
            return null
        }

        private fun ByteArray.toHexString(): String {
            return joinToString(":") { "%02X".format(it) }
        }
    }

    fun requestAuth(context: BaseActivity, cb: (Boolean)->Unit) {
        val intent = HuaweiHiHealth.getSettingController(context).requestAuthorizationIntent(scopes, true)
        context.launcher.launch(intent) {
            val result = HuaweiHiHealth.getSettingController(context).parseHealthKitAuthResultFromIntent(it.data)
            cb(result?.isSuccess ?: false)
        }
    }

    fun checkAuth(context: Context, cb: (Boolean)->Unit) {
        HuaweiHiHealth.getSettingController(context).healthAppAuthorization
            .addOnFailureListener {
                cb(false)
            }
            .addOnSuccessListener {
                cb(it)
            }
    }

    fun cancelAuth(context: Context, cb: (Boolean)->Unit) {
        HuaweiHiHealth.getConsentsController(context).cancelAuthorization(false)
            .addOnFailureListener {
                cb(false)
            }
            .addOnSuccessListener {
                cb(true)
            }
    }

    fun navToAuthDetailPage(context: Context) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val appId = appInfo.metaData.getString(APP_ID_KEY)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$URI_THIRD_PARTY_ACCOUNT_AUTH$appId"))
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun getSleepRecord(context: Context, startTime: Long, endTime: Long, timeout: Int, cb: (data: List<HiHealthSessionData>?, errInfo: String?)->Unit) {
        checkAuth(context = context) { authorized ->
            if (authorized) {
                val hiHealthDataQuery = HiHealthDataQuery(HiHealthSessionType.DATA_SESSION_CORE_SLEEP,
                    startTime, endTime, HiHealthDataQueryOption(),)
                HiHealthDataStore.execQuery(context, hiHealthDataQuery, timeout) { resultCode, data ->
                    when {
                        resultCode == HiHealthError.SUCCESS && data is List<*> -> {
                            if (data.isNotEmpty()) {
                                cb(data.map { e -> e as HiHealthSessionData }, null)
                            } else {
                                cb(null, "Empty sleep data")
                            }
                        }
                        // 参数错误
                        resultCode == HiHealthError.PARAM_INVALID ->
                            cb(null, "Param invalid")
                        // 运动健康版本过低, 不支持此功能
                        resultCode == HiHealthError.ERR_HEALTH_VERSION_IS_NOT_SUPPORTED ->
                            cb(null, "Health application need to be updated")
                        // HMS Core版本过低
                        resultCode == HiHealthError.ERR_HMS_UNAVAILABLE_VERSION ->
                            cb(null, "HMS version is too early")
                        // 授权失效
                        resultCode == HiHealthError.ERR_PERMISSION_EXCEPTION ->
                            cb(null, "Permission denied")
                        // 未同意运动健康隐私协议
                        resultCode == HiHealthError.ERR_PRIVACY_USER_DENIED ->
                            cb(null, "Privacy user denied")
                        // 网络异常
                        resultCode == HiHealthError.ERR_NETWORK ->
                            cb(null, "Network request failed")
                        // 测试权限的用户数量超过限制
                        resultCode == HiHealthError.ERR_BETA_SCOPE_EXCEPTION ->
                            cb(null, "Beta scope permission denied")
                        // 其他错误，建议提示调用失败
                        else ->
                            cb(null, "Other error, invoking method failed")
                    }
                }
            } else cb(null, "Unauthorized")
        }
    }
}