package com.hk.okhttpapplication

import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitServices {

    val userApi: UserServices by lazy {
        return@lazy createRetrofit<UserServices>(createOkHttpClient(), "https://httpbin.org")
    }


    fun changeDomain(apiDomain: String) {
        RetrofitUrlManager.getInstance().putDomain("domain", apiDomain)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return RetrofitUrlManager.getInstance().with(
            OkHttpClient.Builder()
                .addNetworkInterceptor(EncryptInterceptor())
//                .addNetworkInterceptor(StethoInterceptor())
                .addNetworkInterceptor(interceptor)
                .connectTimeout(60L, TimeUnit.SECONDS)
                .readTimeout(60L, TimeUnit.SECONDS)
        ).build()
    }

    private inline fun <reified T> createRetrofit(
        okHttpClient: OkHttpClient,
        serverUrl: String
    ): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(T::class.java)
    }
}