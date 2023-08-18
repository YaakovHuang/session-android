package org.thoughtcrime.securesms.wallet

import kotlinx.coroutines.CoroutineScope
import org.bouncycastle.util.encoders.Hex
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.net.network.CommonClient
import org.thoughtcrime.securesms.util.DeviceUtils
import org.thoughtcrime.securesms.util.KeyStoreUtils
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.coroutine.Coroutine
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by Yaakov on
 * Describe:
 */
object WalletService {

    fun initWallet(seed: String) {
        // init wallet
        val mnemonic = MnemonicUtils.generateMnemonic(Hex.decode(seed))
        val path = intArrayOf(
            44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0
        )
        val seed = MnemonicUtils.generateSeed(mnemonic, "")
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
        val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
        val credentials = Credentials.create(bip44Keypair)
        val pk = Numeric.toHexStringWithPrefix(bip44Keypair.privateKey)
        val address = Keys.toChecksumAddress(credentials.address)
        var wallet = Wallet(0, DeviceUtils.randomKey, KeyStoreUtils.encrypt(mnemonic), KeyStoreUtils.encrypt(pk), address, "")
        AppDataBase.getInstance().walletDao().insert(wallet)
        // init accounts
        val chains = AppDataBase.getInstance().chainDao().loadAll()
        chains?.forEachIndexed { index, chain ->
            var account = Account(key = wallet.key, chain_id = chain.chain_id, address = wallet.address, pk = wallet.pk, isSelect = index == 0)
            AppDataBase.getInstance().accountDao().insert(account)
            // init token
            var token = Token(key = wallet.key!!, chain_id = chain.chain_id, name = chain.currency ?: "", symbol = chain.currency ?: "", icon = chain.icon ?: "", token_type = chain.token_type ?: "", isNative = true, decimals = 18, sort = 999)
            AppDataBase.getInstance().tokenDao().insert(token)
        }

    }

    fun initWallet(scope: CoroutineScope, seed: String): Coroutine<Unit> {
        return Coroutine.async(scope) {
            // init wallet
            val mnemonic = MnemonicUtils.generateMnemonic(Hex.decode(seed))
            val path = intArrayOf(
                44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0
            )
            val seed = MnemonicUtils.generateSeed(mnemonic, "")
            val masterKeyPair = Bip32ECKeyPair.generateKeyPair(Hex.decode(seed))
            val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
            val credentials = Credentials.create(bip44Keypair)
            val pk = Numeric.toHexStringWithPrefix(bip44Keypair.privateKey)
            val address = Keys.toChecksumAddress(credentials.address)
            var wallet = Wallet(0, DeviceUtils.randomKey, KeyStoreUtils.encrypt(mnemonic), KeyStoreUtils.encrypt(pk), address, "")
            AppDataBase.getInstance().walletDao().insert(wallet)

            // init accounts
            val chains = AppDataBase.getInstance().chainDao().loadAll()
            chains?.forEachIndexed { index, chain ->
                var account = Account(key = wallet.key, chain_id = chain.chain_id, address = wallet.address, pk = wallet.pk, isSelect = index == 0)
                AppDataBase.getInstance().accountDao().insert(account)
                // init token
                var token = Token(key = wallet.key!!, chain_id = chain.chain_id, name = chain.currency ?: "", symbol = chain.currency ?: "", icon = chain.icon ?: "", token_type = chain.token_type ?: "", isNative = true, decimals = 18, sort = 999)
                AppDataBase.getInstance().tokenDao().insert(token)
            }
        }.timeout(20000)
    }


    fun getPrivateKey(scope: CoroutineScope, mnemonic: String): Coroutine<String> {
        return Coroutine.async(scope) {
            val path = intArrayOf(44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0)
            val seed = MnemonicUtils.generateSeed(mnemonic, "")
            val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
            val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)
            return@async Numeric.toHexStringWithPrefix(bip44Keypair.privateKey)
        }.timeout(20000)
    }

    fun ethCall(
        chainId: Int,
        fromAddress: String,
        toAddress: String,
        function: Function,
    ): EthResponse {
        val encodedFunction = FunctionEncoder.encode(function)
        val transaction = Transaction.createEthCallTransaction(
            fromAddress, toAddress, encodedFunction
        )
        val ethCall = loadClient(chainId).ethCall(transaction, DefaultBlockParameterName.LATEST).send()
        return if (ethCall.hasError()) {
            Logger.e(Exception("${ethCall.error.code}: ${ethCall.error.message}"))
            EthResponse(
                transactionHash = "", values = listOf(0)
            )
        } else {
            EthResponse(
                transactionHash = "",
                values = ethCall.parseValues(function.outputParameters),
            )
        }
    }

    fun getBalance(
        address: String, token: Token
    ): BigDecimal {
        return try {
            val ethCall = loadClient(token.chain_id).ethGetBalance(address, DefaultBlockParameterName.LATEST).send()
            if (ethCall.hasError()) {
                Logger.e("${ethCall.error.code}: ${ethCall.error.message}")
                BigDecimal.ZERO
            } else {
                BigDecimal(ethCall.balance)
            }
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    fun loadGas(chainId: Int): BigInteger {
        val response = loadClient(chainId).ethGasPrice().send()
        if (response.hasError()) {
            Logger.e("${response.error.code}: ${response.error.message}")
            return BigInteger.ZERO
        }
        return response.gasPrice
    }

    fun loadEstimateGas(
        tx: org.thoughtcrime.securesms.wallet.Transaction
    ): BigInteger {
        if (tx.isNative) {
            return BigInteger.valueOf(21000)
        } else {
            val transaction = Transaction(
                tx.from, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, tx.contractAddress, BigInteger.ZERO, tx.data
            )
            val send = loadClient(tx.chainId).ethEstimateGas(transaction).send()
            return if (send.hasError()) {
                Logger.e("${send.error.code}: ${send.error.message}")
                BigInteger.ZERO
            } else {
                BigDecimal(send.amountUsed).multiply(BigDecimal(1.5)).toBigInteger()
            }
        }
    }

    fun sendTx(
        wallet: Wallet, tx: org.thoughtcrime.securesms.wallet.Transaction
    ): org.thoughtcrime.securesms.wallet.Transaction {
        val nonce = loadClient(tx.chainId).ethGetTransactionCount(tx.from, DefaultBlockParameterName.LATEST).send().transactionCount
        if (Numeric.toBigInt(tx.to) == BigInteger.ZERO) {
            tx.to = ""
        }
        if (tx.data.isNullOrBlank()) {
            tx.data = ""
        }
        val rawTx = RawTransaction.createTransaction(
            nonce, BigInteger(tx.gasPrice), BigInteger(tx.gas), if (tx.contractAddress.isNullOrBlank()) {
                tx.to
            } else {
                tx.contractAddress
            }, BigInteger(tx.value), tx.data
        )
        val signMessage: ByteArray = TransactionEncoder.signMessage(
            rawTx, tx.chainId.toLong(), Credentials.create(KeyStoreUtils.decrypt(wallet.pk))
        )
        val send = loadClient(tx.chainId).ethSendRawTransaction(Numeric.toHexString(signMessage)).send()
        if (send.hasError()) {
            tx.isError = 1
            tx.timeStamp = System.currentTimeMillis() / 1000
            Logger.e(send.error.message)
        } else {
            tx.txreceipt_status = 0
            tx.isError = 0
            tx.hash = send.transactionHash
            tx.timeStamp = System.currentTimeMillis() / 1000
            DaoHelper.insertTx(tx)
        }
        return tx
    }

    fun checkRpcStatus(rpc: String): Boolean {
        var send = Web3j.build(HttpService(rpc)).ethBlockNumber().send()
        return send.hasError()
    }
}

fun loadClient(chainId: Int): Web3j {
    val rpc = DaoHelper.loadSelectRpc(chainId)
    return CommonClient.instance(rpc.rpc)
}

private fun Response<String>.parseValues(outParams: List<TypeReference<Type<*>>>): List<Any> {
    if (outParams.isEmpty()) {
        return emptyList()
    }
    return FunctionReturnDecoder.decode(result, outParams).map { it.value }
}
