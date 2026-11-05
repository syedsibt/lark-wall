package com.aura.wallpapers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.wallpapers.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isLive = 1 ORDER BY displayOrder ASC")
    fun getLiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isLive = 0 ORDER BY displayOrder ASC")
    fun getStaticCategories(): Flow<List<CategoryEntity>>
}
