package org.thoughtcrime.securesms.database.room

import org.thoughtcrime.securesms.wallet.Account
import org.thoughtcrime.securesms.wallet.Token
import org.thoughtcrime.securesms.wallet.Wallet

/**
 * Created by Yaakov on
 * Describe:
 */
object DaoHelper {

    fun loadDefaultWallet(): Wallet {
        return AppDataBase.getInstance().walletDao().loadWallet()
    }

    fun deleteWallet(wallet: Wallet) {
        AppDataBase.getInstance().walletDao().delete(wallet)
    }

    fun loadSelectAccount(): Account {
        return AppDataBase.getInstance().accountDao().loadSelectAccount()
    }

    fun loadTokens(): List<Token> {
        var account = AppDataBase.getInstance().accountDao().loadSelectAccount()
        return AppDataBase.getInstance().tokenDao().loadTokens(account.chain_id!!)
    }


}