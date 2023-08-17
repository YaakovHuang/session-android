package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityWalletBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.et.TokenUpdateEvent
import org.thoughtcrime.securesms.et.WalletUpdateEvent
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.show
import org.thoughtcrime.securesms.util.toastOnUi
import org.thoughtcrime.securesms.wallet.qrcode.QrCodeResult
import org.web3j.crypto.WalletUtils

@AndroidEntryPoint
class WalletActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityWalletBinding

    private val viewModel by viewModels<WalletViewModel>()

    private val adapter by lazy {
        TokenAdapter()
    }

    companion object {
        // Extras
        const val KEY_TOKEN = "token"

    }


    private val qrResult = registerForActivityResult(QrCodeResult()) {
        it ?: return@registerForActivityResult
        if (WalletUtils.isValidAddress(it)) {
            val intent = Intent(this@WalletActivity, SendActivity::class.java)
            intent.putExtra(SendActivity.KEY_TO, it)
            show(intent)
        } else {
            toastOnUi(getString(R.string.address_incorrect))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, true, false, R.color.core_white)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun initViews() {
        super.initViews()
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            updateUI()
            swipeRefreshLayout.setOnRefreshListener {
                initData()
            }
            recyclerView.layoutManager = LinearLayoutManager(this@WalletActivity)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener { adapter, _, position ->
                val token = adapter.data[position] as Token
                val intent = Intent(this@WalletActivity, TokenInfoActivity::class.java)
                intent.putExtra(KEY_TOKEN, token)
                show(intent)
            }
            ivAdd.setOnClickListener {
                var intent = Intent(this@WalletActivity, AddTokenActivity::class.java)
                show(intent)
            }

            llChain.setOnClickListener {
                XPopup.Builder(this@WalletActivity)
                    .asCustom(ChainSelectPopupView(this@WalletActivity) {
                        DaoHelper.updateSelectAccount(it)
                        updateUI()
                        initData()
                    })
                    .show()
            }
            ivView.setOnCheckedChangeListener { _, isChecked ->
                TextSecurePreferences.setHide(this@WalletActivity, isChecked)
                adapter.setHide(isChecked)
                updateUI()
            }
            llWallet.setOnClickListener {
                val intent = Intent(this@WalletActivity, WalletManagerActivity::class.java)
                show(intent)
            }
            llTransfer.setOnClickListener {
                val intent = Intent(this@WalletActivity, SendActivity::class.java)
                show(intent)
            }
            llReceive.setOnClickListener {
                val account = DaoHelper.loadSelectAccount()
                val defaultToken = DaoHelper.loadToken(account.chain_id, true)
                XPopup.Builder(this@WalletActivity)
                    .asCustom(ReceivePopupView(this@WalletActivity, defaultToken))
                    .show()
            }
            llScan.setOnClickListener {
                qrResult.launch(null)
            }

        }
    }

    override fun initData() {
        super.initData()
        viewModel.loadTokens()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.tokensLiveData.observe(this) {
            stopRefreshing(binding.swipeRefreshLayout)
            adapter.data.clear()
            adapter.setNewInstance(it as MutableList<Token>?)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: TokenUpdateEvent) {
        initData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: WalletUpdateEvent) {
        viewModel.wallet.pwd = event.wallet?.pwd ?: ""
    }

    private fun updateUI() {
        var chain = DaoHelper.loadSelectChain()
        GlideHelper.showImage(
            binding.ivLogo,
            chain.icon ?: "",
            100,
            R.drawable.ic_pic_default_round,
            R.drawable.ic_pic_default_round
        )
        binding.ivView.isChecked = TextSecurePreferences.isHide(this)
        binding.tvChainName.text = chain.chain_name
        // TODO:
        binding.tvValue.text = if (TextSecurePreferences.isHide(this)) getString(R.string.content_hide) else "0"
    }

}
