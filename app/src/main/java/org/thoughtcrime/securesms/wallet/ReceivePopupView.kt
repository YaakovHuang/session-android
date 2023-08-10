package org.thoughtcrime.securesms.wallet

import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupReceiveBinding
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.QRCodeUtilities
import org.thoughtcrime.securesms.util.sendToClip
import org.thoughtcrime.securesms.util.toPx

/**
 * Created by Yaakov on
 * Describe:
 */
class ReceivePopupView(
    context: Context,
    private val token: Token
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutPopupReceiveBinding

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_receive
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupReceiveBinding.bind(popupImplView)
        binding.apply {
            val account = DaoHelper.loadSelectAccount()
            tvSymbol.text = "${token.symbol} (${token.network})"
            val size = toPx(120, resources)
            val qrCode = QRCodeUtilities.encode(account.address!!, size, isInverted = false, hasTransparentBackground = false)
            ivQR.setImageBitmap(qrCode)
            tvAddress.text = account.address
            tvOk.setOnClickListener {
                context.sendToClip(account.address)
                dismiss()
            }

        }
    }
}