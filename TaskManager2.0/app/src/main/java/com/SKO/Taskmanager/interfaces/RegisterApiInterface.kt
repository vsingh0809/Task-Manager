package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.RegisterUser
import com.SKO.Taskmanager.responses.StatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterApiInterface {

    @POST("api/User/register")
    fun registerUser(@Body registerUser: RegisterUser): Call<StatusResponse>

}