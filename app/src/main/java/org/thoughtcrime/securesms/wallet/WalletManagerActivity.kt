package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.lxj.xpopup.XPopup
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityWalletManagerBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.formatAddress
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.show

class WalletManagerActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityWalletManagerBinding
    private val viewModel by viewModels<WalletViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityWalletManagerBinding.inflate(layoutInflater)
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
            tvAddress.text = viewModel.wallet.address.formatAddress()
            tvPassword.text = if (viewModel.wallet.pwd.isNullOrEmpty()) getString(R.string.set_password) else getString(R.string.update_password)
            llAddress.setOnClickListener {
                sendToClip(viewModel.wallet.address)
            }
            llUpdatePwd.setOnClickListener {
                if (viewModel.wallet.pwd.isNullOrEmpty()) {
                    val intent = Intent(this@WalletManagerActivity, PasswordActivity::class.java)
                    show(intent)
                } else {
                    val intent = Intent(this@WalletManagerActivity, PasswordUpdateActivity::class.java)
                    show(intent)
                }
            }
            llPk.setOnClickListener {
                XPopup.Builder(this@WalletManagerActivity)
                    .enableDrag(false)
                    .asCustom(PasswordPopupView(this@WalletManagerActivity) {
                        val intent = Intent(this@WalletManagerActivity, ViewPkActivity::class.java)
                        show(intent)
                    }).show()
            }
            llMnemonic.setOnClickListener {
                XPopup.Builder(this@WalletManagerActivity)
                    .enableDrag(false)
                    .asCustom(PasswordPopupView(this@WalletManagerActivity) {
                        val intent = Intent(this@WalletManagerActivity, ViewMnemonicActivity::class.java)
                        show(intent)
                    }).show()
            }
            llRpc.setOnClickListener {
                val intent = Intent(this@WalletManagerActivity, RpcActivity::class.java)
                show(intent)
            }
        }

    }

}
