package com.example.huihutong.api

/**
 * Generic wrapper returned by https://api.215123.cn
 */
data class ApiResponse<T>(
    val code: Int? = null,
    val msg: String? = null,
    val data: T? = null
)

data class TokenData(
    val token: String? = null
)
