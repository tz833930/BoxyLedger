package com.afei.boxyledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.afei.boxyledger.data.model.LedgerRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_records ORDER BY date DESC")
    fun getAllLedgerRecords(): Flow<List<LedgerRecord>>

    @Query("SELECT * FROM ledger_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getLedgerRecordsByDateRange(startDate: Long, endDate: Long): Flow<List<LedgerRecord>>

    @Query("SELECT * FROM ledger_records WHERE id = :id")
    suspend fun getLedgerRecordById(id: Long): LedgerRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerRecord(record: LedgerRecord)

    @Update
    suspend fun updateLedgerRecord(record: LedgerRecord)

    @Delete
    suspend fun deleteLedgerRecord(record: LedgerRecord)
    
    @Query("SELECT SUM(amount) FROM ledger_records WHERE type = 1 AND date >= :startDate AND date <= :endDate")
    fun getIncomeSum(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM ledger_records WHERE type = 0 AND date >= :startDate AND date <= :endDate")
    fun getExpenseSum(startDate: Long, endDate: Long): Flow<Double?>
}
