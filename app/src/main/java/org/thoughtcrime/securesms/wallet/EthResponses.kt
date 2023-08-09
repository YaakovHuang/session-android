package org.thoughtcrime.securesms.wallet

import java.math.BigInteger

data class EthResponse(
    val transactionHash: String,
    val values: List<Any>,
)

data class EthTransactionReceiptResponse(
    val transactionHash: String,
    val transactionIndex: BigInteger,
    val blockHash: String,
    val blockNumber: BigInteger,
    val from: String,
    val to: String,
    val cumulativeGasUsed: BigInteger,
    val gasUsed: BigInteger,
    val contractAddress: String?,
    val status: Boolean,
    val root: String?,
)
