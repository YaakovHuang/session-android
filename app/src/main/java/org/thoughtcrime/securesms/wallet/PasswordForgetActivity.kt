package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityPasswordForgetBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.KeyStoreUtils
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.show
import org.thoughtcrime.securesms.util.toastOnUi

class PasswordForgetActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityPasswordForgetBinding
    private val viewModel by viewModels<WalletViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityPasswordForgetBinding.inflate(layoutInflater)
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
            tvOk.isEnabled = false
            tvOk.setOnClickListener {
                val data = etContent.text.toString().trim()
                if (!data.isNullOrEmpty()) {
                    if (KeyStoreUtils.encrypt(data) == viewModel.wallet.mnemonic) {
                        val intent = Intent(this@PasswordForgetActivity, PasswordActivity::class.java)
                        show(intent)
                    } else if (KeyStoreUtils.encrypt(data) == viewModel.wallet.pk) {
                        val intent = Intent(this@PasswordForgetActivity, PasswordActivity::class.java)
                        show(intent)
                    } else {
                        toastOnUi(getString(R.string.mismatch_key_mismatch))
                    }
                } else {
                    toastOnUi(getString(R.string.content_not_empty))
                }
            }
        }
    }
}
