package org.thoughtcrime.securesms.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.thoughtcrime.securesms.wallet.Token

/**
 * Created by Yaakov on
 * Describe:
 */
@Dao
abstract class TokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg token: Token)

    @Delete
    abstract fun delete(vararg token: Token)

    @Update
    abstract fun update(vararg token: Token)

    @Query("select * from token where `key` = :key")
    abstract fun loadTokens(key: String): List<Token>

    @Query("select * from token where `chain_id` = :chainId")
    abstract fun loadTokens(chainId: Int): List<Token>

    @Query("select * from token where contract = :contract limit 1")
    abstract fun loadToken(contract: String): Token

    @Query("select * from token where chain_id = :chainId and isNative = :isNative limit 1")
    abstract fun loadToken(chainId: Int, isNative: Boolean): Token

    @Query("select * from token")
    abstract fun loadAllTokens(): List<Token>

    @Query("select sum(value) from token where `chain_id` = :chainId")
    abstract fun loadTotalValue(chainId: Int): String

    @Query("select sum(value) from token")
    abstract fun loadTotalValue(): String

}