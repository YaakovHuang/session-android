package org.thoughtcrime.securesms.wallet

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemMnemonicBinding

class MnemonicAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_mnemonic) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemMnemonicBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        with(ItemMnemonicBinding.bind(holder.itemView)) {
            tvNo.text = (holder.adapterPosition + 1).toString()
            tvMnemonic.text = item
        }
    }

}