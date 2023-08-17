package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.viewModels
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityPasswordUpdateBinding
import org.greenrobot.eventbus.EventBus
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.et.WalletUpdateEvent
import org.thoughtcrime.securesms.util.MD5Utils
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.show
import org.thoughtcrime.securesms.util.toastOnUi

class PasswordUpdateActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityPasswordUpdateBinding
    private val viewModel by viewModels<WalletViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityPasswordUpdateBinding.inflate(layoutInflater)
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
            etOriginal.addTextChangedListener(Watcher())
            etNew.addTextChangedListener(Watcher())
            etConfirm.addTextChangedListener(Watcher())
            togglePwdOriginal.setOnCheckedChangeListener { _, isChecked ->
                binding.etOriginal.transformationMethod = if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
                binding.etNew.transformationMethod = if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
                binding.etConfirm.transformationMethod = if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            }
            ivOriginalClear.setOnClickListener {
                binding.etOriginal.text.clear()
            }
            ivNewClear.setOnClickListener {
                binding.etNew.text.clear()
            }
            ivConfirmClear.setOnClickListener {
                binding.etConfirm.text.clear()
            }
            ivForget.setOnClickListener {
                val intent = Intent(this@PasswordUpdateActivity, PasswordForgetActivity::class.java)
                show(intent)
            }
            tvOk.setOnClickListener {
                val pwd1 = etOriginal.text.toString().trim()
                val pwd2 = etNew.text.toString().trim()
                val pwd3 = etConfirm.text.toString().trim()
                if (MD5Utils.md5(pwd1) != viewModel.wallet.pwd) {
                    toastOnUi(getString(R.string.original_incorrect))
                    return@setOnClickListener
                }
                if (pwd2 == pwd3) {
                    val wallet = DaoHelper.loadDefaultWallet()
                    wallet?.run {
                        pwd = MD5Utils.md5(pwd1)
                        DaoHelper.updateWallet(wallet)
                        toastOnUi(getString(R.string.pwd_modify_success))
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
            val pwd1: String = binding.etOriginal.text.toString().trim()
            val pwd2: String = binding.etNew.text.toString().trim()
            val pwd3: String = binding.etConfirm.text.toString().trim()
            binding.ivOriginalClear.visibility = if (TextUtils.isEmpty(pwd1)) View.INVISIBLE else View.VISIBLE
            binding.ivNewClear.visibility = if (TextUtils.isEmpty(pwd2)) View.INVISIBLE else View.VISIBLE
            binding.ivConfirmClear.visibility = if (TextUtils.isEmpty(pwd3)) View.INVISIBLE else View.VISIBLE
            binding.tvOk.isEnabled = !TextUtils.isEmpty(pwd1) && !TextUtils.isEmpty(pwd2) && !TextUtils.isEmpty(pwd3)
        }

        override fun afterTextChanged(s: Editable) {}
    }

}
