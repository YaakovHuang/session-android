package org.thoughtcrime.securesms.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.thoughtcrime.securesms.wallet.Chain

/**
 * Created by Yaakov on
 * Describe:
 */
@Dao
abstract class ChainDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg chain: Chain)

    @Delete
    abstract fun delete(vararg chain: Chain)

    @Update
    abstract fun update(vararg chain: Chain)

    @Query("select * from chain")
    abstract fun loadAll(): List<Chain>

    @Query("select * from chain where chain_id = :chainId limit 1")
    abstract fun loadChain(chainId: Int): Chain

}