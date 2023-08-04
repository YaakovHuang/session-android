package org.thoughtcrime.securesms.net.network

data class BaseWalletResponse<T> (var code: Int, val msg: String, val data: T)