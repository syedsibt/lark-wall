package com.aura.wallpapers.domain.usecase

import com.aura.wallpapers.domain.model.Category
import com.aura.wallpapers.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(liveOnly: Boolean = false, staticOnly: Boolean = false): Flow<List<Category>> {
        return when {
            liveOnly -> categoryRepository.getLiveCategories()
            staticOnly -> categoryRepository.getStaticCategories()
            else -> categoryRepository.getAllCategories()
        }
    }

    suspend fun refresh() {
        categoryRepository.refreshCategories()
    }
}
