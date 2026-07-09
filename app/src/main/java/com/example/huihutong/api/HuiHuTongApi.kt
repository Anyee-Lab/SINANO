package com.example.huihutong.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface HuiHuTongApi {

    @GET("web-app/auth/certificateLogin")
    suspend fun certificateLogin(
        @Query("openId") openId: String
    ): ApiResponse<TokenData>

    @GET("pms/welcome/make-qrcode")
    suspend fun makeQrcode(
        @Header("satoken") satoken: String
    ): ApiResponse<String>
}
