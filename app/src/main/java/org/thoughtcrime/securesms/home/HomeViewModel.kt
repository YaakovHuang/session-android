package org.thoughtcrime.securesms.home

import android.app.Application
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.wallet.WalletService

class HomeViewModel(application: Application) : BaseViewModel(application) {
    fun initWallet() {
        execute {
            val wallet = AppDataBase.getInstance().walletDao().loadWallet()
            if (wallet == null) {
                var seed = IdentityKeyUtil.retrieve(context, IdentityKeyUtil.LOKI_SEED)
                WalletService.initWallet(this, seed)
            }
        }.onSuccess {}.onError {
            Logger.e(it.message)
        }
    }
}