package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.ResetPassword
import com.SKO.Taskmanager.responses.StatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ResetPasswordApiInterface {

    @POST("api/User/forgot-password")
    fun resetPassword(@Body resetPassword : ResetPassword) : Call<StatusResponse>
}