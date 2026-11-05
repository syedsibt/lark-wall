package com.aura.wallpapers.data.remote.interceptor

import com.aura.wallpapers.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("apikey", Constants.DEFAULT_ANON_KEY)
            .header("Authorization", "Bearer ${Constants.DEFAULT_ANON_KEY}")
            .header("Accept", "application/json")
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }
}
