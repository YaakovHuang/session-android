package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityViewMnemonicBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.KeyStoreUtils
import org.thoughtcrime.securesms.util.StatusBarUtil

class ViewMnemonicActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityViewMnemonicBinding
    private val viewModel by viewModels<WalletViewModel>()

    private val adapter by lazy {
        MnemonicAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityViewMnemonicBinding.inflate(layoutInflater)
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
            recyclerView.layoutManager = GridLayoutManager(this@ViewMnemonicActivity, 3)
            binding.recyclerView.adapter = adapter
            val mnemonic = KeyStoreUtils.decrypt(viewModel.wallet.mnemonic)
            val items = mnemonic.split(" ")
            adapter.setNewInstance(items as MutableList<String>)
        }

    }

}
