package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.TokenResponse
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface RefreshTokenApiInterface {

    @POST("auth/refresh")
    fun refreshToken(@Header("Authorization") refreshToken: String): Call<TokenResponse>

}