package org.thoughtcrime.securesms.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityAddTokenBinding
import org.greenrobot.eventbus.EventBus
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.et.TokenUpdateEvent
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.toastOnUi

@AndroidEntryPoint
class AddTokenActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivityAddTokenBinding

    private val viewModel by viewModels<WalletViewModel>()
    private var token: Token? = null
    private val account by lazy {
        DaoHelper.loadSelectAccount()
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityAddTokenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.commonToolbarColor))
        token = intent.parcelable(WalletActivity.KEY_TOKEN)
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
            tvSave.setOnClickListener {
                val contract = etContract.text.toString().trim()
                var symbol = etSymbol.text.toString().trim()
                var decimals = etDecimal.text.toString().trim()
                if (TextUtils.isEmpty(contract) || TextUtils.isEmpty(symbol) || TextUtils.isEmpty(decimals)) {
                    toastOnUi(R.string.no_data)
                    return@setOnClickListener
                }
                if (token == null) {
                    val localToken = DaoHelper.loadToken(contract)
                    if (localToken != null) {
                        toastOnUi(R.string.currency_exist_error)
                    } else {
                        var chain = DaoHelper.loadSelectChain()
                        var token = Token(key = account.key, chain_id = account.chain_id, name = symbol, symbol = symbol, contract = contract, icon = "", token_type = chain.token_type ?: "", isNative = false, decimals = decimals.toInt(), sort = 0)
                        DaoHelper.insertToken(token)
                        EventBus.getDefault().post(TokenUpdateEvent(null))
                        finish()
                    }
                } else {
                    token!!.symbol = symbol
                    token!!.decimals = decimals.toInt()
                    DaoHelper.updateToken(token!!)
                    EventBus.getDefault().post(TokenUpdateEvent(null))
                    finish()
                }
            }

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
