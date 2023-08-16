package org.thoughtcrime.securesms.wallet

import android.graphics.Color
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import network.qki.messenger.R
import network.qki.messenger.databinding.ItemRpcBinding

class RPCAdapter : BaseQuickAdapter<Rpc, BaseViewHolder>(R.layout.item_rpc) {

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        ItemRpcBinding.bind(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: Rpc) {
        with(ItemRpcBinding.bind(holder.itemView)) {
            tvName.text = item.name
            tvContent.text = item.rpc
            tvDelay.text = item.delayTime.toString() + "ms"
            group.visibility = if (item.delayTime == 0L) View.GONE else View.VISIBLE
            progressBar.visibility = if (item.delayTime == 0L) View.VISIBLE else View.GONE
            if (item.isSelect) {
                clContent.setBackgroundResource(R.drawable.selector_rpc_select_20)
            } else {
                clContent.setBackgroundResource(R.drawable.selector_rpc_unselect_20)
            }
            if (item.delayTime <= 300) {
                ivPoint.setColorFilter(Color.GREEN)
            } else if (item.delayTime <= 1000) {
                ivPoint.setColorFilter(Color.YELLOW)
            } else {
                ivPoint.setColorFilter(Color.RED)
            }
        }
    }

}