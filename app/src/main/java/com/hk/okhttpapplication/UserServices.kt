package com.hk.okhttpapplication

import retrofit2.Response
import retrofit2.http.*

interface UserServices {
    @FormUrlEncoded
    @POST("/anything")
    suspend fun getUserInfo(
        @Field("token") token: String
    ): Response<String>
}