package org.thoughtcrime.securesms.net.network

import okhttp3.MultipartBody
import org.thoughtcrime.securesms.constants.AppConst
import org.thoughtcrime.securesms.et.Comment
import org.thoughtcrime.securesms.et.Create
import org.thoughtcrime.securesms.et.ET
import org.thoughtcrime.securesms.et.Nonce
import org.thoughtcrime.securesms.et.User
import org.thoughtcrime.securesms.et.UserInfo
import org.thoughtcrime.securesms.wallet.AppConfig
import org.thoughtcrime.securesms.wallet.Token
import org.thoughtcrime.securesms.wallet.Transaction

/**
 * Created by Yaakov on
 * Describe:
 */
class ApiService {

    val api: Api by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        NetworkApi.INSTANCE.getApi(Api::class.java, AppConst.URLS.BASE)
    }

    suspend fun loadET(cursor: String = ""): List<ET>? {
        return api.loadET(cursor).Data
    }

    suspend fun loadComments(address: String, page: Int): List<Comment>? {
        return api.loadComments(address, page).Data
    }

    suspend fun loadNonce(address: String, sign: String): Nonce? {
        return api.loadNonce(address, sign).Data
    }

    suspend fun authorize(nonce: String, sign: String, address: String): User? {
        return api.authorize(nonce, sign, address).Data
    }

    suspend fun releaseComment(address: String, content: String): BaseResponse<Unit?> {
        return api.releaseComment(address, content)
    }

    suspend fun create(content: String, attachment: String = "", forwardId: String = ""): BaseResponse<Create?> {
        return api.create(content, attachment, forwardId)
    }

    suspend fun release(id: String, sign: String): BaseResponse<Unit?> {
        return api.release(id, sign)
    }

    suspend fun uploadFile(part: MultipartBody.Part): IpfsResponse? {
        return api.uploadFile(part)
    }

    suspend fun loadETFollow(cursor: String = ""): List<ET>? {
        return api.loadETFollow(cursor).Data
    }

    suspend fun loadUserInfo(address: String): UserInfo? {
        return api.loadUserInfo(address).Data
    }

    suspend fun loadETTimeline(address: String): List<ET>? {
        return api.loadETTimeline(address).Data
    }

    suspend fun follow(address: String): BaseResponse<Unit?> {
        return api.follow(address)
    }

    suspend fun cancelFollow(address: String): BaseResponse<Unit?> {
        return api.cancelFollow(address)
    }

    suspend fun loadFollowing(page: Int): List<User>? {
        return api.loadFollowing(page).Data
    }

    suspend fun loadFollowers(page: Int): List<User>? {
        return api.loadFollowers(page).Data
    }

    suspend fun like(tvAddress: String): BaseResponse<Unit?> {
        return api.like(tvAddress)
    }

    suspend fun updateUser(avatar: String, nickname: String, desc: String, sex: String, sign: String, updateSignUnix: String): BaseResponse<Unit?> {
        return api.updateUser(avatar, nickname, desc, sex, sign, updateSignUnix)
    }

    suspend fun loadConfig(deviceId: String, model: String, source: String): AppConfig? {
        return api.loadConfig(deviceId, model, source).data
    }

    suspend fun loadTransactions(address: String, token: Token, page: Int): List<Transaction>? {
        val txs = if (token.isNative) {
            when (token.chain_id) {
                AppConst.CHAIN_IDS.ETH -> api.loadEthNormalTransactions(address, AppConst.API_KEY.ETHSCAN, page, 10).result
                AppConst.CHAIN_IDS.OP -> api.loadOpNormalTransactions(address, AppConst.API_KEY.OPSCAN, page, 10).result
                AppConst.CHAIN_IDS.BSC -> api.loadBscNormalTransactions(address, AppConst.API_KEY.BSCSCAN, page, 10).result
                AppConst.CHAIN_IDS.MATIC -> api.loadMaticNormalTransactions(address, AppConst.API_KEY.MATICSCAN, page, 10).result
                AppConst.CHAIN_IDS.ARB -> api.loadArbNormalTransactions(address, AppConst.API_KEY.ARBSCAN, page, 10).result
                else -> api.loadEthNormalTransactions(address, AppConst.API_KEY.ETHSCAN, page, 10).result
            }
        } else {
            when (token.chain_id) {
                AppConst.CHAIN_IDS.ETH -> api.loadErc20Transactions(address, token.contract, AppConst.API_KEY.ETHSCAN, page, 10).result
                AppConst.CHAIN_IDS.OP -> api.loadOpErc20Transactions(address, token.contract, AppConst.API_KEY.ETHSCAN, page, 10).result
                AppConst.CHAIN_IDS.BSC -> api.loadBscErc20Transactions(address, token.contract, AppConst.API_KEY.ETHSCAN, page, 10).result
                AppConst.CHAIN_IDS.MATIC -> api.loadMaticErc20Transactions(address, token.contract, AppConst.API_KEY.ETHSCAN, page, 10).result
                AppConst.CHAIN_IDS.ARB -> api.loadErc20Transactions(address, token.contract, AppConst.API_KEY.ETHSCAN, page, 10).result
                else -> api.loadErc20Transactions(address, token.contract, AppConst.API_KEY.ETHSCAN, page, 10).result
            }
        }.let {
            for (tx in it) {
                if (token.isNative) {
                    tx.tokenName = token.name
                    tx.tokenSymbol = token.symbol
                    tx.tokenDecimal = token.decimals
                }
                tx.showDecimals = AppConst.SHOW_DECIMAL
                tx.isNative = token.isNative
                tx.chainId = token.chain_id
            }
            it
        }
        return txs
    }
}