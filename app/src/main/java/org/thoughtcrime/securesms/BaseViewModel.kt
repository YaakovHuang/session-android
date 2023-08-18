package org.thoughtcrime.securesms

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.database.room.DaoHelper
import org.thoughtcrime.securesms.util.Logger
import org.thoughtcrime.securesms.util.coroutine.Coroutine
import org.thoughtcrime.securesms.wallet.WalletService
import kotlin.coroutines.CoroutineContext

/**
 * Created by Yaakov on
 * Describe:
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    val context: Context by lazy { this.getApplication<ApplicationContext>() }

    var initWalletLiveData = MutableLiveData<Boolean?>()

    init {
        initWallet {  }
    }

    val wallet by lazy {
        DaoHelper.loadDefaultWallet()
    }

    fun <T> execute(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ): Coroutine<T> {
        return Coroutine.async(scope, context) { block() }
    }

    fun <R> submit(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> Deferred<R>
    ): Coroutine<R> {
        return Coroutine.async(scope, context) { block().await() }
    }


    fun initWallet(onStart: () -> Unit) {
        execute {
            val chains = DaoHelper.loadAllChains()
            if (!chains.isNullOrEmpty()) {
                val wallet = DaoHelper.loadDefaultWallet()
                if (wallet == null) {
                    var seed = IdentityKeyUtil.retrieve(context, IdentityKeyUtil.LOKI_SEED)
                    WalletService.initWallet(seed)
                }
                true
            } else {
                false
            }
        }.onStart {
            onStart.invoke()
        }.onSuccess {
            initWalletLiveData.postValue(it)
        }.onError {
            Logger.e(it.message)
        }
    }

    fun initWallet(seed: String) {
        execute {
            val chains = DaoHelper.loadAllChains()
            if (!chains.isNullOrEmpty()) {
                val wallet = DaoHelper.loadDefaultWallet()
                if (wallet == null) {
                    WalletService.initWallet(seed)
                }
                true
            } else {
                false
            }
        }.onSuccess {
            initWalletLiveData.postValue(true)
        }.onError {
            initWalletLiveData.postValue(false)
            Logger.e(it.message)
        }
    }

}