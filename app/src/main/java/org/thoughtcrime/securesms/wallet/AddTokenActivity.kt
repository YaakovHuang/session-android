package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityWalletBinding
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.StatusBarUtil

@AndroidEntryPoint
class AddTokenActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityWalletBinding

    private val viewModel by viewModels<WalletViewModel>()

    private val adapter by lazy {
        TokenAdapter()
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
            swipeRefreshLayout.setOnRefreshListener {
                initData()
            }
            recyclerView.layoutManager = LinearLayoutManager(this@AddTokenActivity)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener { adapter, _, position ->
                val token = adapter.data[position] as Token
//                val intent = Intent(context, ETDetailActivity::class.java)
//                intent.putExtra(ETFragment.KEY_ET, et)
//                show(intent)
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


}
