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
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Response
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Numeric

/**
 * Created by Yaakov on
 * Describe:
 */
object WalletService {

    fun initWallet(seed: String) {
        // init wallet
        val mnemonic = MnemonicUtils.generateMnemonic(Hex.decode(seed))
        val path = intArrayOf(
            44 or HARDENED_BIT,
            60 or HARDENED_BIT,
            0 or HARDENED_BIT,
            0,
            0
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
            var token = Token(key = wallet.key, chain_id = chain.chain_id, name = chain.name, symbol = chain.chain_symbol, icon = chain.icon, isNative = true, decimals = 18, sort = 999)
            AppDataBase.getInstance().tokenDao().insert(token)
        }

    }

    fun initWallet(scope: CoroutineScope, seed: String): Coroutine<Unit> {
        return Coroutine.async(scope) {
            // init wallet
            val mnemonic = MnemonicUtils.generateMnemonic(Hex.decode(seed))
            val path = intArrayOf(
                44 or HARDENED_BIT,
                60 or HARDENED_BIT,
                0 or HARDENED_BIT,
                0,
                0
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
                var token = Token(key = wallet.key, chain_id = chain.chain_id, name = chain.name, symbol = chain.chain_symbol, icon = chain.icon, isNative = true, decimals = 18, sort = 999)
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
                transactionHash = "",
                values = listOf(0)
            )
        } else {
            EthResponse(
                transactionHash = "",
                values = ethCall.parseValues(function.outputParameters),
            )
        }
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
    Logger.d(result)
    return FunctionReturnDecoder.decode(result, outParams).map { it.value }
}
