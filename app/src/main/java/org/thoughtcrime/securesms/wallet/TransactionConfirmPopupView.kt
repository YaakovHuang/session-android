package org.thoughtcrime.securesms.wallet

import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupTransactionConfirmBinding
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.formatAddress
import java.math.BigDecimal

/**
 * Created by Yaakov on
 * Describe:
 */
class TransactionConfirmPopupView(
    context: Context,
    private val nativeToken: Token,
    private val tx: Transaction,
    private val onNext: (tx: Transaction) -> Unit,
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutPopupTransactionConfirmBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_transaction_confirm
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupTransactionConfirmBinding.bind(popupImplView)
        binding.apply {
            tvAmount.text = EthereumUtil.format(BigDecimal(if (tx.isNative) tx.value else tx.tokenValue), tx.tokenDecimal, AppConst.SHOW_DECIMAL)
            tvSymbol.text = tx.tokenSymbol
            tvType.text = context.getString(R.string.transfer_2)
            tvTo.text = tx.to?.formatAddress()
            tvGas.text = EthereumUtil.format(BigDecimal(tx.gasPrice).multiply(BigDecimal(tx.gas)), nativeToken.decimals, AppConst.SHOW_DECIMAL)
            tvGasDetail.text = "Gas Price(${EthereumUtil.fromWei(BigDecimal(tx.gasPrice), EthereumUtil.Unit.GWEI)}GWEI)*GAS(${tx.gas})"
            tvOk.setOnClickListener {
                onNext.invoke(tx)
                dismiss()
            }
        }

    }
}