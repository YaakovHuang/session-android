package org.thoughtcrime.securesms.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.thoughtcrime.securesms.wallet.Transaction

/**
 * Created by Yaakov on
 * Describe:
 */
@Dao
abstract class TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg tx: Transaction)

    @Delete
    abstract fun delete(vararg tx: Transaction)

    @Update
    abstract fun update(vararg tx: Transaction)

    @Query("select * from `transaction` where `from` = :address or `to` = :address and chainId = :chainId and isNative = :isNative")
    abstract fun loadTxs(address: String, chainId: Int, isNative: Boolean): List<Transaction>

}