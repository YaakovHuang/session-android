package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.XPopup
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityWalletBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.show

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

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, true, false, R.color.core_white)
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
                // TODO:  
                TextSecurePreferences.setHide(this@WalletActivity, isChecked)
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

    private fun updateUI() {
        var chain = DaoHelper.loadSelectChain()
        GlideHelper.showImage(
            binding.ivLogo,
            chain.icon ?: "",
            100,
            R.drawable.ic_pic_default_round,
            R.drawable.ic_pic_default_round
        )
        binding.tvChainName.text = chain.chain_name
    }

}
