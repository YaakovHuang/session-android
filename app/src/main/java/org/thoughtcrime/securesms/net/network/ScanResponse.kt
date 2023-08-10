package org.thoughtcrime.securesms.net.network

/**
 * Created by Author on 2020/4/26
 */
data class ScanResponse<T>(var status: Int, val message: String, val result: T)

