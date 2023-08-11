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
class TokenSelectPopupView(
    context: Context,
    private var token: Token,
    private val onNext: (token: Token) -> Unit
) :
    BottomPopupView(context) {

    lateinit var binding: LayoutPopupChainSelectBinding

    private val adapter by lazy {
        TokenSelectAdapter().apply {
            token = this@TokenSelectPopupView.token
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.layout_popup_chain_select
    }

    override fun onCreate() {
        super.onCreate()
        binding = LayoutPopupChainSelectBinding.bind(popupImplView)
        binding.apply {
            val tokens = DaoHelper.loadTokens()
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.setNewInstance(tokens.toMutableList())
            adapter.setOnItemClickListener { adapter, _, position ->
                val token = adapter.data[position] as Token
                this@TokenSelectPopupView.adapter.token = token
                adapter.notifyDataSetChanged()
            }
            tvOk.setOnClickListener {
                onNext.invoke(adapter.token!!)
                dismiss()
            }

        }
    }
}