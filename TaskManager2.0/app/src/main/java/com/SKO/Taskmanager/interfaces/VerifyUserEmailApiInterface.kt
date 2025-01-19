package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.SendOtpRequest
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.VerifyEmailResponse
import com.SKO.Taskmanager.responses.VerifyOtpRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VerifyUserEmailApiInterface {

    @GET("api/User/emailExists")
    fun verifyEmail(@Query("email") email: String): Call<VerifyEmailResponse>

    @POST("api/Email/sendOtp")
    fun sendOtp(@Body sendOtpRequest: SendOtpRequest) : Call<StatusResponse>

    @POST("api/Otp/validate-otp")
    fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest) : Call<String>
}