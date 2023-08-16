package org.thoughtcrime.securesms.wallet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.core.view.isGone
import com.lxj.xpopup.XPopup
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivitySendBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsession.utilities.getColorFromAttr
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.et.WalletUpdateEvent
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.GlideHelper
import org.thoughtcrime.securesms.util.StatusBarUtil
import org.thoughtcrime.securesms.util.formatAddress
import org.thoughtcrime.securesms.util.openUrl
import org.thoughtcrime.securesms.util.parcelable
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.show
import org.thoughtcrime.securesms.util.toastOnUi
import org.thoughtcrime.securesms.wallet.qrcode.QrCodeResult
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal

class SendActivity : PassphraseRequiredActionBarActivity() {

    private lateinit var binding: ActivitySendBinding

    private val viewModel by viewModels<WalletViewModel>()
    private lateinit var nativeToken: Token
    private lateinit var token: Token
    private lateinit var tx: Transaction
    private var isFirst = true

    private val adapter by lazy { TransactionAdapter() }

    private val account by lazy { DaoHelper.loadSelectAccount() }

    private val qrResult = registerForActivityResult(QrCodeResult()) {
        it ?: return@registerForActivityResult
        if (WalletUtils.isValidAddress(it)) {
            binding.etTo.setText(it)
        } else {
            toastOnUi(getString(R.string.address_incorrect))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StatusBarUtil.setStatusColor(this, false, TextSecurePreferences.CLASSIC_DARK != TextSecurePreferences.getThemeStyle(this), getColorFromAttr(R.attr.commonToolbarColor))
        EventBus.getDefault().register(this)
        nativeToken = DaoHelper.loadToken(account.chain_id, true)
        intent.parcelable<Token>(WalletActivity.KEY_TOKEN)?.let {
            token = it
        }
        if (!this::token.isInitialized) {
            token = nativeToken
            intent.getStringExtra("to")?.let {
                binding.etTo.setText(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun initViews() {
        super.initViews()
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        with(binding) {
            tvAppName.text = getString(R.string.transfer)
            tvOk.isEnabled = false
            tvAddress.isGone = token?.isNative == true
            tvSymbol.text = token!!.symbol
            tvAddress.text = token!!.contract.formatAddress()
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.pageNum = 1
                initData()
            }
            etTo.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    val data = s.toString()
                    if (data.isNullOrEmpty()) {
                        return
                    }
                    val amount = etAmount.text.toString().trim()
                    tvOk.isEnabled = WalletUtils.isValidAddress(data) && amount.isNotEmpty()

                }

            })
            etAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    val data = s.toString()
                    if (data.isNullOrEmpty()) {
                        return
                    }
                    val to = etTo.text.toString().trim()
                    tvOk.isEnabled = WalletUtils.isValidAddress(to)
                }

            })
            tvAddress.setOnClickListener {
                token?.run {
                    if (isNative) {
                        sendToClip(contract)
                    }
                }

            }
            tvAll.setOnClickListener {
                token?.run {
                    val gas = viewModel.gasLiveData.value
                    var total = BigDecimal(viewModel.tokenLiveData.value?.balance)
                    if (isNative) {
                        val fee = BigDecimal(gas).multiply(BigDecimal(21000))
                        total = total.subtract(fee)
                    }
                    if (total > BigDecimal.ZERO) {
                        etAmount.setText(EthereumUtil.format(total, decimals, AppConst.SHOW_DECIMAL))
                    } else {
                        etAmount.setText("0")
                    }
                }

            }
            ivScan.setOnClickListener {
                qrResult.launch(null)
            }
            llToken.setOnClickListener {
                XPopup.Builder(this@SendActivity)
                    .asCustom(TokenSelectPopupView(this@SendActivity, token!!) {
                        token = it
                        updateUI()
                        initData()
                    })
                    .show()
            }
            tvOk.setOnClickListener {
                val to = binding.etTo.text.toString().trim()
                val amount = binding.etAmount.text.toString().trim()
                val tx = viewModel.createTx(token!!, to, amount)
                tx?.let {
                    viewModel.loadGasAndLimit(tx, {
                        showLoading()
                    }, {
                        tx.gasPrice = it.first.toString()
                        tx.gas = it.second.toString()
                        val fee = BigDecimal(tx.gasPrice).multiply(BigDecimal(tx.gas))
                        if (tx.isNative) {
                            if (BigDecimal(tx.value).add(fee) > BigDecimal(token?.balance)) {
                                toastOnUi(String.format(getString(R.string.balance_unenough), nativeToken.symbol))
                                return@loadGasAndLimit
                            }
                        } else {
                            if (fee > BigDecimal(nativeToken.balance)) {
                                toastOnUi(String.format(getString(R.string.balance_unenough), nativeToken.symbol))
                                return@loadGasAndLimit
                            }
                            if (BigDecimal(tx.tokenValue) > BigDecimal(token.balance)) {
                                toastOnUi(
                                    String.format(
                                        getString(R.string.balance_unenough),
                                        token.symbol
                                    )
                                )
                                return@loadGasAndLimit
                            }
                        }
                        fee.compareTo(BigDecimal(nativeToken.balance))
                        XPopup.Builder(this@SendActivity)
                            .enableDrag(false)
                            .asCustom(TransactionConfirmPopupView(
                                this@SendActivity, nativeToken, tx
                            ) {
                                this@SendActivity.tx = tx
                                if (viewModel.wallet.pwd.isNullOrEmpty()) {
                                    val intent = Intent(this@SendActivity, PasswordActivity::class.java)
                                    show(intent)
                                } else {
                                    XPopup.Builder(this@SendActivity)
                                        .enableDrag(false)
                                        .asCustom(PasswordPopupView(this@SendActivity) {
                                            sexTx()
                                        }).show()
                                }
                            })
                            .show()
                    }, {
                        hideLoading()
                    })
                }
            }
            updateUI()

        }
    }

    override fun initData() {
        super.initData()
        viewModel.loadBalance(token!!, {
            if (isFirst) {
                showLoading()
            }
        }, {
            isFirst = false
            hideLoading()
        })
        viewModel.loadTxs(token!!)
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.tokenLiveData.observe(this) {
            stopRefreshing(binding.swipeRefreshLayout)
            token = it

        }
        viewModel.txsLiveData.observe(this) {
            stopRefreshing(binding.swipeRefreshLayout)
            if (viewModel.pageNum == 1) {
                adapter.data.clear()
            }
            adapter.loadMoreModule.isEnableLoadMore = true
            if (it.isNullOrEmpty()) {
                adapter.loadMoreModule.loadMoreEnd()
            } else {
                adapter.loadMoreModule.loadMoreComplete()
                adapter.addData(it)
            }
            if (!it.isNullOrEmpty()) {
                viewModel.pageNum++
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: WalletUpdateEvent) {
        viewModel.wallet = DaoHelper.loadDefaultWallet()
    }


    private fun updateUI() {
        with(binding) {
            GlideHelper.showImage(
                ivLogo, token?.icon ?: "", 100, R.drawable.ic_pic_default_round, R.drawable.ic_pic_default_round
            )
            tvSymbol.text = "${token?.symbol}"
            tvAddress.text = "${viewModel.wallet.address}"
            tvBalance.text = EthereumUtil.format(BigDecimal(token?.balance), token?.decimals ?: 0, AppConst.SHOW_DECIMAL)
        }
    }

    private fun sexTx() {
        viewModel.senTx(tx, {
            showLoading()
        }, { it ->
            if (it.isError == 0) {
                XPopup.Builder(this@SendActivity)
                    .asCustom(
                        TransferSuccessPopupView(this@SendActivity, it, {
                        }, {
                            var chain = DaoHelper.loadSelectChain()
                            val url = chain.browser + "/tx/" + it.hash
                            openUrl(url)
                        })
                    )
                    .show()
            } else {
                toastOnUi(getString(R.string.send_failed))
            }


        }, { hideLoading() })
    }


}
