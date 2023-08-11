package org.thoughtcrime.securesms.wallet

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.lxj.xpopup.core.CenterPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupWalletPasswordBinding
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.MD5Utils
import org.thoughtcrime.securesms.util.toastOnUi

class PasswordPopupView(
    context: Context,
    private val onNext: () -> Unit
) : CenterPopupView(context) {

    private val wallet by lazy { DaoHelper.loadDefaultWallet() }

    lateinit var binding: LayoutPopupWalletPasswordBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_wallet_password
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupWalletPasswordBinding.bind(popupImplView)
        initView()
    }


    private fun initView() {
        with(binding) {
            tvOk.isEnabled = false
            etPwd1.addTextChangedListener(Watcher())
            togglePwd.setOnCheckedChangeListener { _, isChecked ->
                etPwd1.transformationMethod = if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            }
            ivClear1.setOnClickListener {
                etPwd1.text.clear()
            }
            tvOk.setOnClickListener {
                val pwd1 = etPwd1.text.toString().trim()
                if (MD5Utils.md5(pwd1) != wallet?.pwd) {
                    context.toastOnUi(context.getString(R.string.password_error))
                    return@setOnClickListener
                }
                onNext.invoke()
                dismiss()
            }
        }


    }

    inner class Watcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            val pwd1: String = binding.etPwd1.text.toString().trim()
            binding.ivClear1.visibility = if (TextUtils.isEmpty(pwd1)) View.INVISIBLE else View.VISIBLE
            binding.tvOk.isEnabled = !TextUtils.isEmpty(pwd1)
        }

        override fun afterTextChanged(s: Editable) {}
    }


}