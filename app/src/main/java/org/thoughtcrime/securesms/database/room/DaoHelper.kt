package org.thoughtcrime.securesms.database.room

import org.thoughtcrime.securesms.wallet.Account
import org.thoughtcrime.securesms.wallet.Chain
import org.thoughtcrime.securesms.wallet.Rpc
import org.thoughtcrime.securesms.wallet.Token
import org.thoughtcrime.securesms.wallet.Transaction
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

    fun updateWallet(wallet: Wallet) {
        AppDataBase.getInstance().walletDao().update(wallet)
    }

    fun loadSelectAccount(): Account {
        return AppDataBase.getInstance().accountDao().loadSelectAccount()
    }

    fun loadTokens(): List<Token> {
        var account = AppDataBase.getInstance().accountDao().loadSelectAccount()
        return AppDataBase.getInstance().tokenDao().loadTokens(account.chain_id!!)
    }

    fun loadAllTokens(): List<Token> {
        return AppDataBase.getInstance().tokenDao().loadAllTokens()
    }

    fun loadTotalValue(): String {
        return AppDataBase.getInstance().tokenDao().loadTotalValue()
    }

    fun loadTotalValue(chainId: Int): String {
        return AppDataBase.getInstance().tokenDao().loadTotalValue(chainId)
    }

    fun updateTokens(tokens: List<Token>) {
        return AppDataBase.getInstance().tokenDao().update(*tokens.toTypedArray())
    }

    fun loadToken(contract: String): Token {
        return AppDataBase.getInstance().tokenDao().loadToken(contract)
    }

    fun loadToken(chainId: Int, isNative: Boolean): Token {
        return AppDataBase.getInstance().tokenDao().loadToken(chainId, isNative)
    }

    fun insertToken(token: Token) {
        return AppDataBase.getInstance().tokenDao().insert(token)
    }

    fun updateToken(token: Token) {
        return AppDataBase.getInstance().tokenDao().update(token)
    }

    fun loadSelectRpc(chainId: Int): Rpc {
        var rpc = AppDataBase.getInstance().rpcDao().loadSelectRpc(chainId)
        if (rpc == null) {
            rpc = AppDataBase.getInstance().rpcDao().loadRpcsByChainId(chainId)[0]
        }
        return rpc
    }

    fun loadRpcsByChainId(chainId: Int): List<Rpc> {
        return AppDataBase.getInstance().rpcDao().loadRpcsByChainId(chainId)
    }

    fun updateSelectRpc(rpc: Rpc) {
        val rpcs = AppDataBase.getInstance().rpcDao().loadRpcsByChainId(rpc.chainId!!)
        var selectRpc: Rpc? = null
        rpcs.forEach { it ->
            if (it.id == rpc.id) {
                selectRpc = it
            }
            if (it.isSelect) {
                it.isSelect = false
                AppDataBase.getInstance().rpcDao().update(it)
            }
        }
        selectRpc?.let {
            it.isSelect = true
            AppDataBase.getInstance().rpcDao().update(it)
        }
    }


    fun loadSelectChain(): Chain {
        val account = AppDataBase.getInstance().accountDao().loadSelectAccount()
        return AppDataBase.getInstance().chainDao().loadChain(account.chain_id!!)
    }

    fun loadAllChains(): List<Chain> {
        return AppDataBase.getInstance().chainDao().loadAll()
    }

    fun updateSelectAccount(chain: Chain) {
        val accounts = AppDataBase.getInstance().accountDao().loadAll()
        var selectAccount: Account? = null
        accounts.forEach { account ->
            if (account.chain_id == chain.chain_id) {
                selectAccount = account
            }
            if (account.isSelect) {
                account.isSelect = false
                AppDataBase.getInstance().accountDao().update(account)
            }
        }
        selectAccount?.let {
            it.isSelect = true
            AppDataBase.getInstance().accountDao().update(it)
        }
    }

    fun insertTx(tx: Transaction) {
        AppDataBase.getInstance().transactionDao().insert(tx)
    }

    fun deleteTx(tx: Transaction) {
        AppDataBase.getInstance().transactionDao().delete(tx)
    }

    fun loadTxs(address: String, chainId: Int, isNative: Boolean): List<Transaction> {
        return AppDataBase.getInstance().transactionDao().loadTxs(address, chainId, isNative)
    }
}