package org.thoughtcrime.securesms.util

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

/**
 * Created by Author on 2020/5/27
 */
/**
 * Ethereum unit conversion functions.
 */
object EthereumUtil {
    fun fromWei(number: String?, unit: Unit): BigDecimal {
        return fromWei(BigDecimal(number), unit)
    }

    fun fromWei(number: BigDecimal, unit: Unit): BigDecimal {
        return number.divide(unit.weiFactor)
    }

    fun toWei(number: String?, unit: Unit): BigDecimal {
        return toWei(BigDecimal(number), unit)
    }

    fun toWei(number: BigDecimal, unit: Unit): BigDecimal {
        return number.multiply(unit.weiFactor)
    }

    fun fromWei(number: String?, decimal: Int): BigDecimal {
        return fromWei(BigDecimal(number), decimal)
    }

    fun fromWei(number: BigDecimal, decimal: Int): BigDecimal {
        return number.divide(BigDecimal.TEN.pow(decimal))
    }

    fun toWei(number: String?, decimal: Int): BigDecimal {
        return toWei(BigDecimal(number), decimal)
    }

    fun toWei(number: BigDecimal, decimal: Int): BigDecimal {
        return number.multiply(BigDecimal.TEN.pow(decimal))
    }

    fun format(amount: BigDecimal, decimal: Int, showDecimal: Int): String? {
        val unit = BigDecimal(10.0.pow(decimal.toDouble()))
        val rst = amount.divide(unit, decimal, BigDecimal.ROUND_DOWN)
            .setScale(showDecimal, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
        return if (rst.contains(".")) rst else "$rst.00"
    }

    enum class Unit(name: String, factor: Int) {
        WEI("wei", 0), KWEI("kwei", 3), MWEI("mwei", 6), GWEI("gwei", 9), SZABO(
            "szabo",
            12
        ),
        FINNEY("finney", 15), ETHER("ether", 18), KETHER("kether", 21), METHER(
            "mether",
            24
        ),
        GETHER("gether", 27), BTC("btc", 8);

        val weiFactor: BigDecimal

        init {
            weiFactor = BigDecimal.TEN.pow(factor)
        }

        override fun toString(): String {
            return name
        }

        companion object {
            fun fromString(name: String?): Unit {
                if (name != null) {
                    for (unit in values()) {
                        if (name.equals(unit.name, ignoreCase = true)) {
                            return unit
                        }
                    }
                }
                return valueOf(name!!)
            }
        }
    }
}