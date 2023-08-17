package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityPasswordBinding
import org.greenrobot.eventbus.EventBus
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.et.WalletUpdateEvent
import org.thoughtcrime.securesms.util.MD5Utils
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.toastOnUi

class PasswordActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
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
            etPwd.addTextChangedListener(Watcher())
            etPwd2.addTextChangedListener(Watcher())
            etPwd.setOnFocusChangeListener { _, hasFocus ->
                binding.clPwd.isSelected = hasFocus
                if (hasFocus) {
                    binding.etPwd.hint = ""
                } else {
                    binding.etPwd.setHint(R.string.hint_input_wallet_password)
                }
            }
            etPwd2.setOnFocusChangeListener { _, hasFocus ->
                binding.clPwd2.isSelected = hasFocus
                if (hasFocus) {
                    binding.etPwd2.hint = ""
                } else {
                    binding.etPwd2.setHint(R.string.hint_input_wallet_password_too)
                }
            }
            togglePwd.setOnCheckedChangeListener { _, isChecked ->
                binding.etPwd.transformationMethod = if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
                binding.etPwd2.transformationMethod = if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            }
            ivClear.setOnClickListener {
                binding.etPwd.text.clear()
            }
            ivClear2.setOnClickListener {
                binding.etPwd2.text.clear()
            }
            tvOk.setOnClickListener {
                val pwd1 = etPwd.text.toString().trim()
                val pwd2 = etPwd2.text.toString().trim()
                if (pwd1 == pwd2) {
                    val wallet = DaoHelper.loadDefaultWallet()
                    wallet?.run {
                        pwd = MD5Utils.md5(pwd1)
                        DaoHelper.updateWallet(wallet)
                        toastOnUi(getString(R.string.password_set_success))
                        EventBus.getDefault().post(WalletUpdateEvent(DaoHelper.loadDefaultWallet()))
                        finish()
                    }
                } else {
                    toastOnUi(getString(R.string.twice_password_diff))
                }
            }
        }

    }

    inner class Watcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val pwd1: String = binding.etPwd.text.toString().trim()
            val pwd2: String = binding.etPwd2.text.toString().trim()
            binding.ivClear.visibility = if (TextUtils.isEmpty(pwd1)) View.INVISIBLE else View.VISIBLE
            binding.ivClear2.visibility = if (TextUtils.isEmpty(pwd2)) View.INVISIBLE else View.VISIBLE
            binding.tvOk.isEnabled = !TextUtils.isEmpty(pwd1) && !TextUtils.isEmpty(pwd2)
        }

        override fun afterTextChanged(s: Editable) {}
    }

}
