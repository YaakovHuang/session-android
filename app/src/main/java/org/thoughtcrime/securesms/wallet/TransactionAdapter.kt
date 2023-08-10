package org.thoughtcrime.securesms.wallet

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemTransactionBinding
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.formatAddress
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by Author on 2023/8/10
 */
class TransactionAdapter : BaseQuickAdapter<Transaction, BaseViewHolder>(R.layout.item_transaction), LoadMoreModule {

    private val isHide by lazy {
        TextSecurePreferences.isHide(context)
    }
    private val account by lazy {
        DaoHelper.loadSelectAccount()
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemTransactionBinding.bind(viewHolder.itemView)
    }

    override fun convert(helper: BaseViewHolder, tx: Transaction) {
        val binding = ItemTransactionBinding.bind(helper.itemView)
        account?.run {
            var prefix: String
            if (address.equals(tx.to, true)) {
                prefix = "+"
                if (tx.isError == 1) {
                    binding.ivStatus.setImageResource(R.drawable.ic_transfer_failed)
                } else {
                    if ((if (tx.confirmations == null) 0 else tx.confirmations)!! >= AppConst.CONFIRMATIONS) {
                        binding.ivStatus.setImageResource(R.drawable.ic_transfer_in)
                    } else {
                        binding.ivStatus.setImageResource(R.drawable.ic_transfer_wailt)
                    }
                }
            } else {
                prefix = "-"
                if (tx.isError == 1) {
                    binding.ivStatus.setImageResource(R.drawable.ic_transfer_failed)
                } else {
                    if ((if (tx.confirmations == null) 0 else tx.confirmations)!! >= AppConst.CONFIRMATIONS) {
                        binding.ivStatus.setImageResource(R.drawable.ic_transfer_out)
                    } else {
                        binding.ivStatus.setImageResource(R.drawable.ic_transfer_wailt)
                    }
                }
            }
            binding.tvDate.text = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(tx.timeStamp * 1000))
            binding.tvAmount.text = "${prefix}${
                EthereumUtil.format(
                    BigDecimal(tx.value), tx.tokenDecimal, AppConst.SHOW_DECIMAL
                )
            } ${tx.tokenSymbol}"
            binding.tvAddress.text = tx.to?.formatAddress() ?: ""
        }

    }

}
