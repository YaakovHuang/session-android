package org.thoughtcrime.securesms.wallet

import kotlinx.coroutines.CoroutineScope
import org.session.libsignal.utilities.Hex
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.util.DeviceUtils
import org.thoughtcrime.securesms.util.KeyStoreUtils
import org.thoughtcrime.securesms.util.coroutine.Coroutine
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric

/**
 * Created by Yaakov on
 * Describe:
 */
object WalletService {

    fun initWallet(scope: CoroutineScope, seed: String): Coroutine<Unit> {
        return Coroutine.async(scope) {
            // init wallet
            val mnemonic = MnemonicUtils.generateMnemonic(Hex.fromStringCondensed(seed))
            val path = intArrayOf(
                44 or HARDENED_BIT,
                60 or HARDENED_BIT,
                0 or HARDENED_BIT,
                0,
                0
            )
            val masterKeyPair = Bip32ECKeyPair.generateKeyPair(Hex.fromStringCondensed(seed))
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
}