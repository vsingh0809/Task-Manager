package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.LoginUser
import com.SKO.Taskmanager.responses.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiInterface {

    @POST("api/User/login")
    fun login(@Body loginUser: LoginUser): Call<TokenResponse>

}