package org.thoughtcrime.securesms.wallet

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemChainBinding
import org.thoughtcrime.securesms.util.GlideHelper

class TokenSelectAdapter : BaseQuickAdapter<Token, BaseViewHolder>(R.layout.item_chain) {

    var token:Token?= null

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemChainBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Token) {
        with(ItemChainBinding.bind(holder.itemView)) {
            GlideHelper.showImage(
                ivLogo,
                item.icon ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvName.text = item.contract
            tvSymbol.text = item.symbol
            cbSelect.isChecked = token?.contract.equals(item.contract, true)
        }
    }

}