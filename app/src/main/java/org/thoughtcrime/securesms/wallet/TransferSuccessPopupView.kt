package org.thoughtcrime.securesms.wallet

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupTransferSuccessBinding

/**
 * Created by Yaakov on
 * Describe:
 */
class TransferSuccessPopupView(
    context: Context,
    private val tx: Transaction,
    private val onNext: (tx: Transaction) -> Unit,
    private val onView: (tx: Transaction) -> Unit
) : CenterPopupView(context) {

    lateinit var binding: LayoutPopupTransferSuccessBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_transfer_success
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupTransferSuccessBinding.bind(popupImplView)
        binding.apply {
            // TODO:  
            tvView.text = "Go to Bscscan"
            tvOk.setOnClickListener {
                onNext.invoke(tx)
                dismiss()
            }
            tvView.setOnClickListener {
                onView.invoke(tx)
                dismiss()
            }
        }


    }
}