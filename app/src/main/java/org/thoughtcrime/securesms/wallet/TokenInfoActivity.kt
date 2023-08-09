package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityTokenInfoBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.toastOnUi

class TokenInfoActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityTokenInfoBinding

    private val viewModel by viewModels<WalletViewModel>()
    private var token: Token? = null
    private val account by lazy {
        DaoHelper.loadSelectAccount()
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityTokenInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.chatsToolbarColor))
        token = intent.parcelable(WalletActivity.KEY_TOKEN)
        token?.let {

        } ?: finish()
    }

    override fun initViews() {
        super.initViews()
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            tvAppName.text = if (token == null) {
                getString(R.string.add_custom_token)
            } else {
                getString(R.string.edit)
            }
            token?.apply {
                etContract.setText(contract)
                etSymbol.setText(symbol)
                etDecimal.setText(decimals)
            }
            etContract.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val contract = s.toString().trim()
                    if (!TextUtils.isEmpty(contract)) {
                        viewModel.loadDecimals(account.chain_id!!, account.address!!, contract)
                        viewModel.loadSymbol(account.chain_id!!, account.address!!, contract)
                    }
                }

            })
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.decimalLiveData.observe(this) {
            binding.etDecimal.setText(it)
        }
        viewModel.symbolLiveData.observe(this) {
            binding.etSymbol.setText(it)
        }
        viewModel.errorCode.observe(this) {
            when (it) {
                -1 -> toastOnUi(R.string.customize_currency_contract_error)
                -2 -> toastOnUi(R.string.currency_exist_error)
            }
        }
    }


}
