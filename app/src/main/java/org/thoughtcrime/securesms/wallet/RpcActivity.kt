package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityRpcBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.StatusBarUtil

class RpcActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityRpcBinding
    private val viewModel by viewModels<WalletViewModel>()

    private val adapter by lazy {
        RPCAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityRpcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.commonToolbarColor))
    }

    override fun initViews() {
        super.initViews()
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(this@RpcActivity)
            binding.recyclerView.adapter = adapter
            adapter.setOnItemClickListener { adapter, _, position ->
                val rpc = adapter.data[position] as Rpc
                DaoHelper.updateSelectRpc(rpc)
                initData()
            }

        }

    }

    override fun initData() {
        super.initData()
        val account = DaoHelper.loadSelectAccount()
        val rpcs = DaoHelper.loadRpcsByChainId(account.chain_id)
        viewModel.checkRpcDelay(rpcs)
        adapter.setNewInstance(rpcs as MutableList<Rpc>)
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.rpcDelayLiveData.observe(this) {
            adapter.notifyDataSetChanged()
        }
    }

}
