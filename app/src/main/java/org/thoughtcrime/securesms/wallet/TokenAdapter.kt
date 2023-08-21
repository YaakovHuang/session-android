package org.thoughtcrime.securesms.wallet

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemTokenBinding
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.GlideHelper
import java.math.BigDecimal

class TokenAdapter : BaseQuickAdapter<Token, BaseViewHolder>(R.layout.item_token) {


    private var isHide: Boolean = false

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemTokenBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Token) {
        with(ItemTokenBinding.bind(holder.itemView)) {
            GlideHelper.showImage(
                ivLogo,
                item.icon,
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvName.text = item.symbol
            if (isHide) {
                tvPrice.text = context.getString(R.string.content_hide)
                tvAmount.text = context.getString(R.string.content_hide)
                tvValue.text = context.getString(R.string.content_hide)
            } else {
                tvPrice.text = "\$ 0"
                tvAmount.text = "${EthereumUtil.format(BigDecimal(item.balance), item.decimals, AppConst.SHOW_DECIMAL)}"
                tvValue.text = "\$ ${item.value}"
            }
        }
    }

    fun setHide(isHide: Boolean) {
        this.isHide = isHide
        notifyDataSetChanged()
    }

}