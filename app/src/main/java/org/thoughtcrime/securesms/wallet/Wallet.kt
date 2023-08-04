package org.thoughtcrime.securesms.wallet

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by Yaakov on
 * Describe:
 */
@Entity
@Parcelize
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var key: String?,
    var mnemonic: String? = null,
    var pk: String? = null,
    var address: String,
    var pwd: String?
) : Parcelable

@Entity
@Parcelize
data class Account(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var key: String?,
    var chain_id: Int?,
    var address: String?,
    var pk: String?,
    var isSelect: Boolean = false,
    var sort: Int = 0,
    // u本位总价值
    var value: String = ""
) : Parcelable

@Entity
@Parcelize
data class Token(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var key: String?,
    var chain_id: Int?,
    var name: String?,
    var symbol: String?,
    var contract: String= "",
    var icon: String?,
    var price: String = "0",
    var isNative: Boolean = false,
    var decimals: Int = 0,
    var balance: String = "0",
    // u本位总价值
    var value: String = "",
    var sort: Int = 0
) : Parcelable

@Parcelize
data class AppConfig(
    var config: Config?,
    var network: List<Chain>?
) : Parcelable

@Parcelize
data class Config(
    var website: String?,
) : Parcelable

@Entity
@Parcelize
data class Chain(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var chain_id: Int?,
    var name: String?,
    var chain_name: String?,
    var chain_symbol: String?,
    var browser: String?,
    var token_type: String?,
    var currency: String?,
    var sort: Int?,
    var is_default: String?,
    var is_customize: Int?,
    var icon: String?,
    var currency_icon: String?
) : Parcelable {
    @Ignore
    var rpc: List<Rpc>? = null
}

@Entity
@Parcelize
data class Rpc(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String?,
    var rpc: String?,
    var symbol: String?,
    var chainId: Int?,
    var customize: Boolean = false,
    var status: Boolean = true,
    var isSelect: Boolean = false,
    var sort: Int = 0
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other !is Rpc) return false
        return other.rpc == rpc

    }
}

