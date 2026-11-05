package com.aura.wallpapers.domain.repository

import com.aura.wallpapers.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getStaticCategories(): Flow<List<Category>>
    fun getLiveCategories(): Flow<List<Category>>
    suspend fun refreshCategories()
}
