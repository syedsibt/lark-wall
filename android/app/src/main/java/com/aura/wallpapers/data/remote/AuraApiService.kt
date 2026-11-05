package com.aura.wallpapers.data.remote

import com.aura.wallpapers.data.remote.dto.CategoryDto
import com.aura.wallpapers.data.remote.dto.LiveWallpaperDto
import com.aura.wallpapers.data.remote.dto.WallpaperDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuraApiService {
    @GET("wallpapers")
    suspend fun getWallpapers(
        @Query("select") select: String = "*,categories(id,name,slug)",
        @Query("is_published") isPublished: String = "eq.true",
        @Query("order") order: String = "download_count.desc",
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("category_id") categoryId: String? = null,
        @Query("is_featured") isFeatured: String? = null
    ): List<WallpaperDto>

    @GET("live_wallpapers")
    suspend fun getLiveWallpapers(
        @Query("select") select: String = "*,categories(id,name,slug)",
        @Query("is_published") isPublished: String = "eq.true",
        @Query("order") order: String = "download_count.desc",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("category_id") categoryId: String? = null,
        @Query("is_featured") isFeatured: String? = null
    ): List<LiveWallpaperDto>

    @GET("categories")
    suspend fun getCategories(
        @Query("select") select: String = "*",
        @Query("order") order: String = "display_order.asc"
    ): List<CategoryDto>

    @POST("rpc/increment_download")
    suspend fun incrementDownloadCount(
        @Body payload: IncrementDownloadRequest
    ): Response<Unit>
}

data class IncrementDownloadRequest(
    val p_asset_id: String,
    val p_asset_type: String // "static" or "live"
)
