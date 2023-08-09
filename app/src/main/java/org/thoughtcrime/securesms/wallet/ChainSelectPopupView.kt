package org.thoughtcrime.securesms.wallet

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.lxj.xpopup.core.BottomPopupView
import network.qki.messenger.R
import network.qki.messenger.databinding.LayoutPopupChainSelectBinding
import org.thoughtcrime.securesms.database.room.DaoHelper

/**
 * Created by Yaakov on
 * Describe:
 */
class ChainSelectPopupView(
    context: Context,
    private val onNext: (chain: Chain) -> Unit
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutPopupChainSelectBinding

    private val adapter by lazy {
        ChainSelectAdapter()
    }

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_chain_select
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupChainSelectBinding.bind(popupImplView)
        binding.apply {
            val chains = DaoHelper.loadAllChains()
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.setNewInstance(chains.toMutableList())
            adapter.setOnItemClickListener { adapter, _, position ->
                val chain = adapter.data[position] as Chain
                this@ChainSelectPopupView.adapter.chain = chain
                adapter.notifyDataSetChanged()
            }
            tvOk.setOnClickListener {
                onNext.invoke(adapter.chain)
                dismiss()
            }

        }
    }
}