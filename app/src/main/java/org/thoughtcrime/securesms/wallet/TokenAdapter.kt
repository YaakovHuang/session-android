package org.thoughtcrime.securesms.wallet

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemTokenAssetBinding
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.GlideHelper
import java.math.BigDecimal

class TokenAdapter : BaseQuickAdapter<Token, BaseViewHolder>(R.layout.item_token_asset) {


    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemTokenAssetBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Token) {
        with(ItemTokenAssetBinding.bind(holder.itemView)) {
            GlideHelper.showImage(
                ivLogo,
                item.icon ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvName.text = item.symbol
            tvPrice.text = "\$ 0"
            tvAmount.text = "${EthereumUtil.format(BigDecimal(item.balance), item.decimals, AppConst.SHOW_DECIMAL)}"
            tvValue.text = "\$ ${item.value}"
        }
    }

}