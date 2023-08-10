package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import androidx.activity.viewModels
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivitySendBinding
import network.qki.messenger.databinding.LayoutTokenInfoHeaderBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.formatAddress
import org.thoughtcrime.securesms.util.parcelable
import java.math.BigDecimal

class SendActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivitySendBinding

    private val viewModel by viewModels<WalletViewModel>()
    private var token: Token? = null
    private var isFirst = true

    private val adapter by lazy { TransactionAdapter() }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.commonToolbarColor))
        token = intent.parcelable(WalletActivity.KEY_TOKEN)
        token?.let {

        } ?: finish()
    }

    override fun initViews() {
        super.initViews()
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            tvAppName.text = getString(R.string.transfer)
            tvOk.isEnabled = false
            tvSymbol.text = token!!.symbol
            tvAddress.text = token!!.contract.formatAddress()
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.pageNum = 1
                initData()
            }

        }
    }

    override fun initData() {
        super.initData()
        viewModel.loadBalance(token!!, {
            if (isFirst) {
                showLoading()
            }
        }, {
            isFirst = false
            hideLoading()
        })
        viewModel.loadTxs(token!!)
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.tokenLiveData.observe(this) {
            stopRefreshing(binding.swipeRefreshLayout)
            token = it

        }
        viewModel.txsLiveData.observe(this) {
            stopRefreshing(binding.swipeRefreshLayout)
            if (viewModel.pageNum == 1) {
                adapter.data.clear()
            }
            adapter.loadMoreModule.isEnableLoadMore = true
            if (it.isNullOrEmpty()) {
                adapter.loadMoreModule.loadMoreEnd()
            } else {
                adapter.loadMoreModule.loadMoreComplete()
                adapter.addData(it)
            }
            if (!it.isNullOrEmpty()) {
                viewModel.pageNum++
            }
        }
    }


    private fun updateUI(binding: LayoutTokenInfoHeaderBinding) {
        with(binding) {
            GlideHelper.showImage(
                ivLogo, token?.icon ?: "", 100, R.drawable.ic_pic_default_round, R.drawable.ic_pic_default_round
            )
            tvSymbol.text = "${token?.symbol}"
            tvPrice.text = "\$ ${token?.price}"
            tvAddress.text = "${viewModel.wallet.address}"
            tvBalance.text = EthereumUtil.format(BigDecimal(token?.balance), token?.decimals ?: 0, AppConst.SHOW_DECIMAL)
            tvValue.text = "\$${token?.value}"
        }
    }


}
