package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import androidx.activity.viewModels
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityViewPkBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.KeyStoreUtils
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.sendToClip

class ViewPkActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityViewPkBinding
    private val viewModel by viewModels<WalletViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityViewPkBinding.inflate(layoutInflater)
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
            tvPk.text = KeyStoreUtils.decrypt(viewModel.wallet.pk)
            tvOk.setOnClickListener {
                sendToClip(KeyStoreUtils.decrypt(viewModel.wallet.pk))
            }
        }

    }

}
