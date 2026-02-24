package com.afei.boxyledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.afei.boxyledger.data.model.Account
import com.afei.boxyledger.data.model.Category
import com.afei.boxyledger.data.model.LedgerRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.firstOrNull

@Database(entities = [Account::class, LedgerRecord::class, Category::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE accounts ADD COLUMN isCredit INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: android.content.Context): AppDatabase {
            return Instance ?: synchronized(this) {
                androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, "boxy_ledger_database")
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate categories
                            Instance?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateCategories(database.categoryDao())
                                    populateAccounts(database.accountDao())
                                }
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
        
        suspend fun populateAccounts(dao: AccountDao) {
            // Check if any account exists
            if (dao.getAllAccounts().firstOrNull()?.isEmpty() == true) {
                val accounts = listOf(
                    Account(name = "钱包现金", type = "现金账户", balance = 0.0, icon = "AccountBalanceWallet"),
                    Account(name = "中国银行", type = "银行账户", balance = 0.0, icon = "AccountBalance"),
                    Account(name = "支付宝", type = "网络账户", balance = 0.0, icon = "Smartphone"),
                    Account(name = "微信", type = "网络账户", balance = 0.0, icon = "Chat")
                )
                accounts.forEach { dao.insertAccount(it) }
            }
        }

        suspend fun populateCategories(dao: CategoryDao) {
            if (dao.getCategoryCount() == 0) {
                // Expense Categories
                val expenseCats = listOf(
                    "餐饮" to "Restaurant", "交通" to "DirectionsCar", "购物" to "ShoppingBag",
                    "娱乐" to "Movie", "住房" to "Home", "医疗" to "LocalHospital",
                    "教育" to "School", "通讯" to "Smartphone", "服装" to "Checkroom",
                    "其他" to "Inventory2"
                )
                expenseCats.forEachIndexed { index, (name, icon) ->
                    dao.insertCategory(Category(name = name, icon = icon, type = 0, sortOrder = index))
                }
                
                // Income Categories
                val incomeCats = listOf(
                    "工资" to "AttachMoney", "奖金" to "CardGiftcard", "投资" to "TrendingUp",
                    "兼职" to "Work", "红包" to "Mail", "退款" to "AssignmentReturn",
                    "其他" to "Diamond"
                )
                incomeCats.forEachIndexed { index, (name, icon) ->
                    dao.insertCategory(Category(name = name, icon = icon, type = 1, sortOrder = index))
                }
            }
        }
    }
}
