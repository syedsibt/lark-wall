package com.aura.wallpapers.domain.usecase

import com.aura.wallpapers.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke(
        assetId: String,
        currentlyFavorite: Boolean,
        isLive: Boolean,
        title: String,
        thumbnailUrl: String
    ) {
        if (currentlyFavorite) {
            favoritesRepository.removeFavorite(assetId)
        } else {
            favoritesRepository.addFavorite(assetId, isLive, title, thumbnailUrl)
        }
    }
}
