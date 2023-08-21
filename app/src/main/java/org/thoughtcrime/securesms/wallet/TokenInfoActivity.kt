package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityTokenInfoBinding
import network.qki.messenger.databinding.LayoutStatelayoutEmptyBinding
import network.qki.messenger.databinding.LayoutTokenInfoHeaderBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.show
import java.math.BigDecimal

class TokenInfoActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityTokenInfoBinding
    private lateinit var headerBinding: LayoutTokenInfoHeaderBinding

    private val viewModel by viewModels<WalletViewModel>()
    private var token: Token? = null
    private var isFirst = true

    private val adapter by lazy { TransactionAdapter() }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityTokenInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.commonToolbarColor))
        token = intent.parcelable(WalletActivity.KEY_TOKEN)
        token?.let {

        } ?: finish()
    }

    override fun onResume() {
        super.onResume()
        initData()
    }

    override fun initViews() {
        super.initViews()
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            tvAppName.text = token?.symbol
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.pageNum = 1
                initData()
            }
            recyclerView.layoutManager = LinearLayoutManager(this@TokenInfoActivity)
            recyclerView.adapter = adapter
            adapter.loadMoreModule.setOnLoadMoreListener {
                viewModel.loadTxs(token!!)
            }
            adapter.loadMoreModule.isAutoLoadMore = true
            adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
            adapter.setOnItemClickListener { adapter, _, position ->
                val tx = adapter.data[position] as Transaction
//                val intent = Intent(context, ETDetailActivity::class.java)
//                intent.putExtra(ETFragment.KEY_ET, et)
//                show(intent)
            }
            initHeader()
            // empty
            val emptyViewBinding = LayoutStatelayoutEmptyBinding.inflate(LayoutInflater.from(this@TokenInfoActivity), root, false)
            adapter.setEmptyView(emptyViewBinding.root)
            val layoutParams: ViewGroup.LayoutParams = emptyViewBinding.root.layoutParams
            layoutParams.height = binding.recyclerView.height - headerBinding.root.height
            adapter.addChildClickViewIds(R.id.llFavorite, R.id.llForward, R.id.ivMore)
            adapter.setOnItemChildClickListener { adapter, v, position -> }
            tvTransfer.setOnClickListener {
                val intent = Intent(this@TokenInfoActivity, SendActivity::class.java)
                intent.putExtra(WalletActivity.KEY_TOKEN, token)
                show(intent)
            }
            tvReceive.setOnClickListener {
                XPopup.Builder(this@TokenInfoActivity)
                    .asCustom(ReceivePopupView(this@TokenInfoActivity, token!!))
                    .show()
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
            updateUI(headerBinding)
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

    private fun initHeader() {
        adapter.headerWithEmptyEnable = true
        headerBinding = LayoutTokenInfoHeaderBinding.inflate(
            LayoutInflater.from(this@TokenInfoActivity), null, false
        )
        adapter.addHeaderView(headerBinding.root)
        updateUI(headerBinding)
    }

    private fun updateUI(binding: LayoutTokenInfoHeaderBinding) {
        with(binding) {
            GlideHelper.showImage(
                ivLogo, token?.icon ?: "", 100, R.drawable.ic_pic_default_round, R.drawable.ic_pic_default_round
            )
            tvSymbol.text = "${token?.symbol}"
            tvAddress.text = "${viewModel.wallet.address}"
            if (TextSecurePreferences.isHide(this@TokenInfoActivity)) {
                tvPrice.text = getString(R.string.content_hide)
                tvBalance.text = getString(R.string.content_hide)
                tvValue.text = getString(R.string.content_hide)
            } else {
                tvPrice.text = "\$ ${token?.price}"
                tvBalance.text = EthereumUtil.format(BigDecimal(token?.balance), token?.decimals ?: 0, AppConst.SHOW_DECIMAL)
                tvValue.text = "\$${token?.value}"
            }
        }
    }


}
