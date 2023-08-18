package org.thoughtcrime.securesms.wallet

import android.app.Application
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import network.qki.messenger.R
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.DeviceUtils
import org.thoughtcrime.securesms.util.EthereumUtil
import org.thoughtcrime.securesms.util.FunctionUtils
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.toastOnUi
import org.web3j.abi.FunctionEncoder
import java.math.BigDecimal
import java.math.BigInteger

class WalletViewModel(application: Application) : BaseViewModel(application) {

    val tokenLiveData = MutableLiveData<Token>()
    val tokensLiveData = MutableLiveData<List<Token>>()
    val txsLiveData = MutableLiveData<List<Transaction>?>()
    val symbolLiveData = MutableLiveData<String>()
    val decimalLiveData = MutableLiveData<String>()
    val gasLiveData = MutableLiveData<BigInteger>()
    val nativeTokenLiveData = MutableLiveData<Token>()
    val saveStatusLiveData = MutableLiveData<Boolean>()
    val rpcDelayLiveData = MutableLiveData<Rpc>()
    var configLiveData = MutableLiveData<AppConfig>()
    var errorCode = MutableLiveData<Int>()

    var pageNum = 1

    private val apiService by lazy {
        ApiService()
    }

    fun loadConfig() {
        execute {
            val config = apiService.loadConfig(DeviceUtils.getAndroidId(context), DeviceUtils.systemModel, "android")
            config?.let {
                dealConfig(it)
            }
            config
        }.onSuccess {
            configLiveData.postValue(it)
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadAllPrices() {
        execute {
            val tokens = DaoHelper.loadAllTokens()
            var symbols = mutableListOf<String>()
            tokens?.forEach { token ->
                if (token.symbol.isNotEmpty()) {
                    symbols.add(token.symbol)
                }
            }
            val prices = apiService.loadTokenPrice(symbols.joinToString(","))
            prices?.forEach {
                for (token in tokens) {
                    if (token.symbol == it.symbol && token.contract.lowercase() == it.contract?.lowercase() && token.chain_id == it.chain_id) {
                        token.price = it.price ?: "0"
                        break
                    }
                }
            }
            DaoHelper.updateTokens(tokens)
        }.onSuccess {
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadNativeBalance(token: Token) {
        execute {
            val ethResponse = WalletService.getBalance(
                wallet.address,
                token
            )
            token.balance = ethResponse.stripTrailingZeros().toPlainString()
            nativeTokenLiveData.postValue(token)
            Logger.d("${token.symbol} = ${token.balance}")
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadBalance(
        token: Token,
        onStart: () -> Unit,
        onFinally: () -> Unit
    ) {
        execute {
            val token = loadTokenBalance(token)
            Logger.d("${token.symbol} = ${token.balance}")
            token
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            tokenLiveData.postValue(it)
        }.onError {
            Logger.e(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun loadLocalTokens() {
        execute {
            DaoHelper.loadTokens()
        }.onSuccess {
            tokensLiveData.postValue(it)
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadTokens() {
        execute {
            var tokens = DaoHelper.loadTokens()
            tokens.map { token ->
                async {
                    var token = loadTokenBalance(token)
                    token.value = EthereumUtil.format(BigDecimal(token.price).multiply(BigDecimal(token.balance)), token.decimals, 2) ?: "0.00"
                    DaoHelper.updateToken(token)
                    token
                }
            }.awaitAll()
        }.onSuccess {
            tokensLiveData.postValue(it)
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadTxs(
        token: Token
    ) {
        execute {
            val txs = apiService.loadTransactions(wallet.address, token, pageNum)
            txs?.forEach {
                it.key = token.key.toString()
            }
            if (pageNum == 1) organizeData(txs, token) else txsLiveData.postValue(txs)
        }.onError {
            Logger.e(it.message)
            if (pageNum == 1) organizeData(null, token) else txsLiveData.postValue(null)

        }
    }

    fun loadSymbol(chainId: Int, from: String, to: String) {
        execute {
            WalletService.ethCall(chainId, from, to, FunctionUtils.encodeSymbol())
        }.onSuccess {
            symbolLiveData.postValue(it.values[0] as String)
        }.onError {
            errorCode.postValue(-1)
            Logger.e(it.message)
        }
    }

    fun loadDecimals(chainId: Int, from: String, to: String) {
        execute {
            WalletService.ethCall(chainId, from, to, FunctionUtils.encodeDecimals())
        }.onSuccess {
            decimalLiveData.postValue((it.values[0] as BigInteger).toString())
        }.onError {
            errorCode.postValue(-1)
            Logger.e(it.message)
        }
    }

    fun loadGas(token: Token) {
        execute {
            val gas = WalletService.loadGas(token.chain_id)
            gasLiveData.postValue(gas)
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadGasAndLimit(
        tx: Transaction,
        onStart: () -> Unit,
        onSuccess: (gasPair: Pair<BigInteger, BigInteger>) -> Unit,
        onFinally: () -> Unit
    ) {
        execute {
            val gas = WalletService.loadGas(tx.chainId)
            val gasLimit = WalletService.loadEstimateGas(tx)
            Pair(gas, gasLimit)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            onSuccess.invoke(it)
        }.onError {
            Logger.e(it.message)
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun createTx(
        token: Token,
        to: String,
        amount: String
    ): Transaction? {
        if (!org.web3j.crypto.WalletUtils.isValidAddress(to)) {
            context.toastOnUi(context.getString(R.string.address_incorrect))
            return null
        }
        if (BigDecimal(amount) <= BigDecimal.ZERO) {
            context.toastOnUi(context.getString(R.string.please_fill_in_correct_amount))
            return null
        }

        var tx = if (token.isNative) {
            Transaction(
                key = token.key,
                tokenName = token.name,
                tokenSymbol = token.symbol,
                tokenDecimal = token.decimals,
                isNative = true,
                chainId = token.chain_id,
                from = wallet.address,
                to = to,
                value = EthereumUtil.toWei(amount, token.decimals).stripTrailingZeros().toPlainString(),
                data = "",
                hash = ""
            )
        } else {
            Transaction(
                key = token.key,
                tokenName = token.name,
                tokenSymbol = token.symbol,
                tokenDecimal = token.decimals,
                tokenValue = EthereumUtil.toWei(amount, token.decimals).stripTrailingZeros().toPlainString(),
                isNative = token.isNative,
                chainId = token.chain_id,
                from = wallet.address,
                to = to,
                contractAddress = token.contract,
                value = "0",
                data = FunctionEncoder.encode(
                    FunctionUtils.encodeTransfer(
                        to,
                        EthereumUtil.toWei(amount, token.decimals).stripTrailingZeros().toPlainString()
                    )
                ),
                hash = ""
            )
        }
        return tx
    }

    fun senTx(
        tx: Transaction,
        onStart: () -> Unit,
        onSuccess: (tx: Transaction) -> Unit,
        onFinally: () -> Unit
    ) {
        execute {
            WalletService.sendTx(wallet, tx)
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            onSuccess.invoke(it)
        }.onError {
            Logger.e(it.cause?.message)
            context.toastOnUi(context.getString(R.string.send_failed))
        }.onFinally {
            onFinally.invoke()
        }
    }

    fun checkRpcDelay(rpcs: List<Rpc>) {
        execute {
            rpcs.forEach {
                try {
                    var startTime = System.currentTimeMillis()
                    var error = WalletService.checkRpcStatus(it.rpc!!)
                    if (!error) {
                        var long = System.currentTimeMillis() - startTime
                        it.delayTime = long
                        rpcDelayLiveData.postValue(it)
                    }
                } catch (e: Exception) {
                    Logger.e(e.message)
                }

            }
        }
    }


    private fun dealConfig(appConfig: AppConfig) {
        val chains = appConfig.network
        chains?.let { chains ->
            var rpcList = arrayListOf<Rpc>()
            chains.forEachIndexed { _, chain ->
                val rpcs = chain.rpc
                rpcs?.let { rpcs ->
                    rpcs.forEachIndexed { _, rpc ->
                        var r = Rpc(name = rpc.name, rpc = rpc.rpc, symbol = chain.chain_symbol, chainId = chain.chain_id)
                        rpcList.add(r)
                    }
                }
            }
            val localRpcs = AppDataBase.getInstance().rpcDao().loadRpcs(false)
            val isDelete = TextSecurePreferences.getDeleteRPC(context)
            if (!isDelete) {
                AppDataBase.getInstance().rpcDao().deleteAllRpcs()
                TextSecurePreferences.setDeleteRPC(context, true)
            }
            rpcList.forEach { rpc ->
                val contains = contains(localRpcs, rpc)
                if (contains) {
                    val rpc1 = AppDataBase.getInstance().rpcDao().loadRpcByRpc(rpc.rpc ?: "")
                    rpc1?.let {
                        rpc.id = rpc1.id
                        rpc.isSelect = rpc1.isSelect
                        AppDataBase.getInstance().rpcDao().update(rpc)
                    }
                } else {
                    AppDataBase.getInstance().rpcDao().insert(rpc)
                }
            }
            localRpcs?.forEach { rpc ->
                val contains = contains(rpcList, rpc)
                if (!contains) {
                    AppDataBase.getInstance().rpcDao().deleteRpc(rpc.rpc ?: "")
                }
            }
            AppDataBase.getInstance().chainDao().delete(*AppDataBase.getInstance().chainDao().loadAll().toTypedArray())
            AppDataBase.getInstance().chainDao().insert(*chains.toTypedArray())
        }

    }

    fun contains(list: List<Rpc>?, rpc: Rpc): Boolean {
        list?.forEach {
            if (it.rpc == rpc.rpc) {
                return true
            }
        }
        return false
    }

    private fun organizeData(txs: List<Transaction>?, token: Token) {
        var oldTxs = DaoHelper.loadTxs(wallet.address, token.chain_id, token.isNative)
        var currentTime = System.currentTimeMillis() / 1000
        var availableTxs = mutableListOf<Transaction>()
        for (tx in oldTxs) {
            if (currentTime - tx.timeStamp > 60 * 60 * 24) {
                DaoHelper.deleteTx(tx)
                continue
            }
            availableTxs.add(tx)
        }
        if (txs.isNullOrEmpty()) {
            txsLiveData.postValue(availableTxs)
            return
        }
        txs.forEach {
            for (tx in oldTxs) {
                if (it.hash == tx.hash) {
                    DaoHelper.deleteTx(tx)
                    availableTxs.remove(tx)
                    break
                }
            }
        }
        availableTxs.addAll(txs)
        txsLiveData.postValue(availableTxs)
    }

    private fun loadTokenBalance(token: Token): Token {
        if (token.isNative) {
            val ethResponse = WalletService.getBalance(wallet.address, token)
            token.balance = ethResponse.stripTrailingZeros().toPlainString()
        } else {
            val ethResponse = WalletService.ethCall(token.chain_id, wallet.address, token.contract, FunctionUtils.encodeBalanceOf(wallet.address))
            token.balance = if (ethResponse.values.isNullOrEmpty()) {
                BigInteger.ZERO.toString()
            } else {
                try {
                    (ethResponse.values[0] as BigInteger).toBigDecimal().stripTrailingZeros().toPlainString()
                } catch (e: Exception) {
                    "0"
                }
            }
        }
        return token
    }
}