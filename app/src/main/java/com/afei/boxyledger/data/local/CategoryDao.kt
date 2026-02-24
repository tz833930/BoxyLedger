package com.afei.boxyledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.afei.boxyledger.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY sortOrder ASC")
    fun getCategoriesByType(type: Int): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE name = :name AND type = :type LIMIT 1")
    suspend fun getCategoryByNameAndType(name: String, type: Int): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
