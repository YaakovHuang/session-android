package org.thoughtcrime.securesms.wallet

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.session.libsession.utilities.TextSecurePreferences
import org.thoughtcrime.securesms.BaseViewModel
import org.thoughtcrime.securesms.database.room.AppDataBase
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.net.network.ApiService
import org.thoughtcrime.securesms.util.DeviceUtils
import org.thoughtcrime.securesms.util.Logger

class WalletViewModel(application: Application) : BaseViewModel(application) {

    val tokensLiveData = MutableLiveData<List<Token>>()

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
            rpcList.forEachIndexed { _, rpc ->
                val contains = localRpcs?.contains(rpc)
                if (contains == true) {
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
                val contains = rpcList.contains(rpc)
                if (!contains) {
                    AppDataBase.getInstance().rpcDao().deleteRpc(rpc.rpc ?: "")
                }
            }
            AppDataBase.getInstance().chainDao().delete(*AppDataBase.getInstance().chainDao().loadAll().toTypedArray())
            AppDataBase.getInstance().chainDao().insert(*chains.toTypedArray())
        }

    }

}