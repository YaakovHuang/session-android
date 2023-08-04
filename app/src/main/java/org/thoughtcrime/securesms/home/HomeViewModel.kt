package org.thoughtcrime.securesms.home

import android.app.Application
import org.session.libsignal.utilities.Hex
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.util.toWallet
import org.web3j.crypto.MnemonicUtils

class HomeViewModel(application: Application) : BaseViewModel(application) {

    fun initWallet() {
        val wallet = AppDataBase.getInstance().walletDao().loadWallet()
        if (wallet == null) {
            var seed = IdentityKeyUtil.retrieve(context, IdentityKeyUtil.LOKI_SEED)
            val mnemonic = MnemonicUtils.generateMnemonic(Hex.fromStringCondensed(seed))
            val wallet = mnemonic.toWallet()
            AppDataBase.getInstance().walletDao().insert(wallet)
        }
    }
}