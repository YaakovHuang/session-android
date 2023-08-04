package org.thoughtcrime.securesms.database.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.thoughtcrime.securesms.wallet.Account

/**
 * Created by Yaakov on
 * Describe:
 */
@Dao
abstract class AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg account: Account)

    @Delete
    abstract fun delete(vararg account: Account)

    @Update
    abstract fun update(vararg account: Account)

    @Query("select * from account")
    abstract fun loadAll(): List<Account>

}