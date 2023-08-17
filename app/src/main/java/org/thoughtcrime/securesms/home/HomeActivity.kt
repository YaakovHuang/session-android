package org.thoughtcrime.securesms.home

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.azhon.appupdate.manager.DownloadManager
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.BuildConfig
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityHomeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.TextSecurePreferences.Companion.getThemeStyle
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.GroupDatabase
import org.thoughtcrime.securesms.database.MmsSmsDatabase
import org.thoughtcrime.securesms.database.RecipientDatabase
import org.thoughtcrime.securesms.database.ThreadDatabase
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.toastOnUi
import org.thoughtcrime.securesms.wallet.WalletViewModel
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var threadDb: ThreadDatabase

    @Inject
    lateinit var mmsSmsDatabase: MmsSmsDatabase

    @Inject
    lateinit var recipientDatabase: RecipientDatabase

    @Inject
    lateinit var groupDatabase: GroupDatabase

    @Inject
    lateinit var textSecurePreferences: TextSecurePreferences

    private val viewModel by viewModels<WalletViewModel>()

    private var lastPressTime: Long = 0

    private val viewPagerAdapter: ManagerViewPagerAdapter by lazy {
        ManagerViewPagerAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?, isReady: Boolean) {
        super.onCreate(savedInstanceState, isReady)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this@HomeActivity, false, TextSecurePreferences.CLASSIC_DARK != getThemeStyle(this@HomeActivity), getColorFromAttr(R.attr.commonToolbarColor))
        binding.viewpager.adapter = viewPagerAdapter
        binding.viewpager.isUserInputEnabled = false
        binding.viewpager.offscreenPageLimit = 2
        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            return@setOnItemSelectedListener when (item.itemId) {
                R.id.menu_chat -> {
                    binding.viewpager.setCurrentItem(0, false)
                    StatusBarUtil.setStatusColor(this@HomeActivity, false, TextSecurePreferences.CLASSIC_DARK != getThemeStyle(this@HomeActivity), getColorFromAttr(R.attr.commonToolbarColor))
                    true
                }

                R.id.menu_et -> {
                    binding.viewpager.setCurrentItem(1, false)
                    StatusBarUtil.setStatusColor(this@HomeActivity, false, TextSecurePreferences.CLASSIC_DARK != getThemeStyle(this@HomeActivity), getColorFromAttr(R.attr.commonToolbarColor))
                    true
                }

                R.id.menu_me -> {
                    binding.viewpager.setCurrentItem(2, false)
                    StatusBarUtil.setStatusColor(this@HomeActivity, true, false, ContextCompat.getColor(this@HomeActivity, R.color.core_white))
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (lastPressTime == 0L || now - lastPressTime > 2 * 1000) {
            toastOnUi(getString(R.string.exit_again))
            lastPressTime = now
        } else if (now - lastPressTime < 2 * 1000) super.onBackPressed()
    }

    override fun initData() {
        checkUpdate()
        loadConfig()
    }

    override fun initObserver() {
        viewModel.configLiveData.observe(this) {
            viewModel.initWallet {}
        }
    }

    private fun checkUpdate() {
        val client = OkHttpClient.Builder()
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            .readTimeout(35000, TimeUnit.MILLISECONDS) // 设置连接时间和读取时间
            .build() // 设置缓存
        val doRequestUrl = BuildConfig.updateServer + "update.json"
        val request = Request.Builder().url(doRequestUrl).get().build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失败回调 回调是在子线程中，可使用Handler、post、activity.runOnUiThread()等方式在主线程中更新ui
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                //成功回调  可使用Handler、post、activity.runOnUiThread()等方式在主线程中更新ui
                //获取返回byte数组
                if (!response.isSuccessful) {
                    throw IOException("Bad response: " + response.message)
                }
                val resultData = response.body!!.string()
                try {
                    val jsonObject = JSONObject(resultData)
                    android.util.Log.d(
                        "checkUpdate",
                        "onResponse: " + jsonObject.getString("versionCode")
                    )
                    update(jsonObject)
                } catch (e: JSONException) {
                    throw RuntimeException(e)
                }
            }
        })
    }

    private fun update(jsonObject: JSONObject) {
        val manager = DownloadManager.Builder(this).run {
            apkUrl(jsonObject.getString("url"))
            apkName(jsonObject.getString("versionName") + ".apk")
            smallIcon(R.drawable.ic_launcher)
            //设置了此参数，那么内部会自动判断是否需要显示更新对话框，否则需要自己判断是否需要更新
            apkVersionCode(jsonObject.getInt("versionCode"))
            //同时下面三个参数也必须要设置
            apkVersionName(jsonObject.getString("versionName"))
            apkSize(jsonObject.getString("apkSize"))
            apkDescription(jsonObject.getString("Description"))
            //省略一些非必须参数...
            build()
        }
        manager?.download()
    }

    private fun loadConfig() {
        viewModel.loadConfig()
    }

    fun showTabLayout(isShow: Boolean) {
        binding.bottomNavigationView.isVisible = isShow
    }


}
