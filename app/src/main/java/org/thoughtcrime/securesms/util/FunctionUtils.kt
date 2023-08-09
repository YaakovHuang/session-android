package org.thoughtcrime.securesms.util

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.Utils
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger
import java.util.Arrays

object FunctionUtils {

    /**
     *
     *
     * @param tokenA
     * @param tokenB
     * @param amountADesired
     * @param amountBDesired
     * @param amountAMin
     * @param amountBMin
     * @param to
     * @param deadline
     * @return
     */
    fun encodeAddLiquidity(
        tokenA: String?,
        tokenB: String?,
        amountADesired: BigInteger?,
        amountBDesired: BigInteger?,
        amountAMin: BigInteger?,
        amountBMin: BigInteger?,
        to: String?,
        deadline: BigInteger?
    ): String {
        val params = Arrays.asList<Type<*>>(
            Address(tokenA),
            Address(tokenB),
            Uint256(amountADesired),
            Uint256(amountBDesired),
            Uint256(amountAMin),
            Uint256(amountBMin),
            Address(to),
            Uint256(deadline)
        )
        val returnTypes = Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        val function = Function("addLiquidity", params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    /**
     *
     *
     * @param tokenA
     * @param tokenB
     * @param liquidity
     * @param amountAMin
     * @param amountBMin
     * @param to
     * @param deadline
     * @return
     */
    fun encodeRemoveLiquidity(
        tokenA: String?,
        tokenB: String?,
        liquidity: BigInteger?,
        amountAMin: BigInteger?,
        amountBMin: BigInteger?,
        to: String?,
        deadline: BigInteger?
    ): String {
        val params = listOf<Type<*>>(
            Address(tokenA),
            Address(tokenB),
            Uint256(liquidity),
            Uint256(amountAMin),
            Uint256(amountBMin),
            Address(to),
            Uint256(deadline)
        )
        val returnTypes = listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        val function = Function("removeLiquidity", params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    /**
     *
     *
     * @param method
     * @param token
     * @param amountTokenDesired
     * @param amountTokenMin
     * @param amountETHMin
     * @param to
     * @param deadline
     * @return
     */
    fun encodeAddLiquidityMainNet(
        method: String?,
        amountTokenDesired: BigInteger?,
        token: String?,
        amountTokenMin: BigInteger?,
        amountETHMin: BigInteger?,
        to: String?,
        deadline: BigInteger?
    ): String {
        val params = listOf<Type<*>>(
            Address(token),
            Uint256(amountTokenDesired),
            Uint256(amountTokenMin),
            Uint256(amountETHMin),
            Address(to),
            Uint256(deadline)
        )
        val returnTypes = listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        val function = Function(method, params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    /**
     *
     *
     * @param method
     * @param liquidity
     * @param token
     * @param amountTokenMin
     * @param amountETHMin
     * @param to
     * @param deadline
     * @return
     */
    fun encodeRemoveLiquidityMainNet(
        method: String?,
        liquidity: BigInteger?,
        token: String?,
        amountTokenMin: BigInteger?,
        amountETHMin: BigInteger?,
        to: String?,
        deadline: BigInteger?
    ): String {
        val params = listOf<Type<*>>(
            Address(token),
            Uint256(liquidity),
            Uint256(amountTokenMin),
            Uint256(amountETHMin),
            Address(to),
            Uint256(deadline)
        )
        val returnTypes = listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        val function = Function(method, params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    fun encodeTotalSupply(): String {
        val function = Function(
            "totalSupply",
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        return FunctionEncoder.encode(function)
    }

    fun encodeAllowance(owner: String?, spender: String?): String {
        val function = Function(
            "allowance",
            listOf<Type<*>>(Address(owner), Address(spender)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        return FunctionEncoder.encode(function)
    }

    fun encodeBalanceOf(address: String): Function {
        return Function(
            "balanceOf",
            listOf<Type<*>>(Address(address)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }

    fun encodeSymbol(): Function {
        return Function(
            "symbol",
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Utf8String>() {})
        )
    }

    fun encodeDecimals(): Function {
        return Function(
            "decimals",
            listOf(),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }

    fun encodeTransfer(address: String, amount: String): Function {
        return Function(
            "transfer",
            listOf<Type<*>>(Address(address), Uint256(BigInteger(amount))),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
    }

    fun encodeApprove(spender: String?, amount: BigInteger?): String {
        val function = Function(
            "approve",
            listOf<Type<*>>(Address(spender), Uint256(amount)),
            listOf<TypeReference<*>>(object : TypeReference<Bool?>() {})
        )
        return FunctionEncoder.encode(function)
    }

    /**
     * 使用代币兑换主网币
     *
     * @param amountIn
     * @param amountOutMin
     * @param path
     * @param to
     * @param deadline
     * @return
     */
    fun encodeSwapExactTokensForETH(
        amountIn: BigInteger?,
        amountOutMin: BigInteger?,
        path: List<String>?,
        to: String?,
        deadline: BigInteger?,
        chainId: Int,
        isFee: Boolean
    ): String {
        val params = Arrays.asList(
            Uint256(amountIn), Uint256(amountOutMin), DynamicArray(
                Address::class.java, Utils.typeMap(path, Address::class.java)
            ), Address(to), Uint256(deadline)
        )
        val returnTypes = emptyList<TypeReference<*>>()
        val funName = "swapExactTokensForETH"
        val function = Function(funName, params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    /**
     * 使用主网币兑换代币
     *
     * @param amountOut
     * @param path
     * @param to
     * @param deadline
     * @return
     */
    fun encodeSwapETHForExactTokens(
        amountOut: BigInteger?,
        path: List<String>?,
        to: String?,
        deadline: BigInteger?,
        chainId: Int
    ): String {
        val params = Arrays.asList(
            Uint256(amountOut), DynamicArray(
                Address::class.java, Utils.typeMap(path, Address::class.java)
            ), Address(to), Uint256(deadline)
        )
        val returnTypes = emptyList<TypeReference<*>>()
        val funName = "swapETHForExactTokens"
        val function = Function(funName, params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    /**
     * 使用代币兑换代币
     *
     * @param amountIn
     * @param amountOutMin
     * @param path
     * @param to
     * @param deadline
     * @return
     */
    fun encodeSwapExactTokensForTokens(
        amountIn: BigInteger?,
        amountOutMin: BigInteger?,
        path: List<String>?,
        to: String?,
        deadline: BigInteger?,
        chainId: Int,
        isFee: Boolean
    ): String {
        val params = Arrays.asList(
            Uint256(amountIn), Uint256(amountOutMin), DynamicArray(
                Address::class.java, Utils.typeMap(path, Address::class.java)
            ), Address(to), Uint256(deadline)
        )
        val returnTypes = emptyList<TypeReference<*>>()
        val funName: String
        funName = if (isFee) {
            "swapExactTokensForTokensSupportingFeeOnTransferTokens"
        } else {
            "swapExactTokensForTokens"
        }
        val function = Function(funName, params, returnTypes)
        return FunctionEncoder.encode(function)
    }

    fun encodeGetAmountsOut(amountIn: BigInteger?, path: List<String>?): Function {
        val params = Arrays.asList(
            Uint256(amountIn), DynamicArray(
                Address::class.java, Utils.typeMap(path, Address::class.java)
            )
        )
        val returnTypes =
            Arrays.asList<TypeReference<*>>(object : TypeReference<DynamicArray<Uint256?>?>() {})
        return Function("getAmountsOut", params, returnTypes)
    }

    fun encodeGetPair(contractA: String?, ContractB: String?): Function {
        val params = Arrays.asList<Type<*>>(Address(contractA), Address(ContractB))
        val returnTypes = Arrays.asList<TypeReference<*>>(object : TypeReference<Address?>() {})
        return Function("getPair", params, returnTypes)
    }

    fun encodeGetReserves(): Function {
        val params = emptyList<Type<*>>()
        val returnTypes = Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {},
            object : TypeReference<Uint256?>() {},
            object : TypeReference<Uint256?>() {})
        return Function("getReserves", params, returnTypes)
    }

    fun encodeToken0(): Function {
        val params = emptyList<Type<*>>()
        val returnTypes = Arrays.asList<TypeReference<*>>(object : TypeReference<Address?>() {})
        return Function("token0", params, returnTypes)
    }

    fun encodeToken1(): Function {
        val params = emptyList<Type<*>>()
        val returnTypes = Arrays.asList<TypeReference<*>>(object : TypeReference<Address?>() {})
        return Function("token1", params, returnTypes)
    }
}