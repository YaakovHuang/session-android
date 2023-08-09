package org.thoughtcrime.securesms.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.thoughtcrime.securesms.wallet.Rpc

/**
 * Created by Yaakov on
 * Describe:
 */
@Dao
abstract class RpcDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(rpc: Rpc)

    @Delete
    abstract fun delete(rpc: Rpc)

    @Update
    abstract fun update(rpc: Rpc)

    @Query("select * from rpc")
    abstract fun loadAllRpcs(): Rpc

    @Query("select * from rpc where customize = :customize")
    abstract fun loadRpcs(customize: Boolean): List<Rpc>?

    @Query("select * from rpc where chainId = :chainId and isSelect = 1 limit 1")
    abstract fun loadSelectRpc(chainId: Int): Rpc

    @Query("select * from rpc where chainId = :chainId")
    abstract fun loadRpcsByChainId(chainId: Int): List<Rpc>

    @Query("delete from rpc where customize = 0")
    abstract fun deleteAllRpcs()

    @Query("select * from rpc where rpc = :rpc")
    abstract fun loadRpcByRpc(rpc: String): Rpc?

    @Query("delete from rpc where rpc = :rpc")
    abstract fun deleteRpc(rpc: String)
}