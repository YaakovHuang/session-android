package org.thoughtcrime.securesms.database.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.wallet.Account
import org.thoughtcrime.securesms.wallet.Chain
import org.thoughtcrime.securesms.wallet.Rpc
import org.thoughtcrime.securesms.wallet.Token
import org.thoughtcrime.securesms.wallet.Wallet

/**
 * Created by Yaakov on
 * Describe:
 */
@Database(
    entities = [Wallet::class, Chain::class, Rpc::class, Account::class, Token::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun rpcDao(): RpcDao
    abstract fun chainDao(): ChainDao
    abstract fun accountDao(): AccountDao
    abstract fun tokenDao(): TokenDao

    companion object {
        private const val DATABASE_NAME = "messenger.db"

        @Volatile
        private var databaseInstance: AppDataBase? = null

        @Synchronized
        open fun getInstance(): AppDataBase {
            if (databaseInstance == null) {
                databaseInstance = Room.databaseBuilder(ApplicationContext.context, AppDataBase::class.java, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build()
            }
            return databaseInstance!!
        }
    }
}
