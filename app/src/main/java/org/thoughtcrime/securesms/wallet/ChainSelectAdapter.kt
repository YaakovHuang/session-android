package org.thoughtcrime.securesms.wallet

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemChainBinding
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.GlideHelper

class ChainSelectAdapter : BaseQuickAdapter<Chain, BaseViewHolder>(R.layout.item_chain) {

    var chain = DaoHelper.loadSelectChain()

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemChainBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Chain) {
        with(ItemChainBinding.bind(holder.itemView)) {
            GlideHelper.showImage(
                ivLogo,
                item.icon ?: "",
                100,
                R.drawable.ic_pic_default_round,
                R.drawable.ic_pic_default_round
            )
            tvName.text = item.chain_name
            tvSymbol.text = item.chain_symbol
            cbSelect.isChecked = chain.chain_id == item.chain_id
        }
    }

}