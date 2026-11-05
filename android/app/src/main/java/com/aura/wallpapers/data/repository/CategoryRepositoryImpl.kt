package com.aura.wallpapers.data.repository

import com.aura.wallpapers.data.local.dao.CategoryDao
import com.aura.wallpapers.data.local.entity.toEntity
import com.aura.wallpapers.data.remote.AuraApiService
import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val apiService: AuraApiService,
    private val categoryDao: CategoryDao,
    private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getStaticCategories(): Flow<List<Category>> {
        return categoryDao.getStaticCategories()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override fun getLiveCategories(): Flow<List<Category>> {
        return categoryDao.getLiveCategories()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun refreshCategories() = withContext(ioDispatcher) {
        runCatching {
            val dts = apiService.getCategories()
            categoryDao.insertCategories(dts.map { it.toDomain().toEntity() })
        }
        Unit
    }
}
