package org.thoughtcrime.securesms.wallet

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.DeviceUtils
import org.thoughtcrime.securesms.util.FunctionUtils
import org.thoughtcrime.securesms.util.Logger
import java.math.BigInteger

class WalletViewModel(application: Application) : BaseViewModel(application) {

    val tokensLiveData = MutableLiveData<List<Token>>()
    val symbolLiveData = MutableLiveData<String>()
    val decimalLiveData = MutableLiveData<String>()
    val saveStatusLiveData = MutableLiveData<Boolean>()
    var errorCode = MutableLiveData<Int>()

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
        }.onError {
            Logger.e(it.message)
        }
    }

    fun initWallet(seed: String) {
        execute {
            WalletService.initWallet(this, seed)
        }.onSuccess {
        }.onError {
            Logger.e(it.message)
        }
    }

    fun loadTokens() {
        execute {
            DaoHelper.loadTokens()
        }.onSuccess {
            tokensLiveData.postValue(it)
        }.onError {
            Logger.e(it.message)
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
            if (isDelete == false) {
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


}