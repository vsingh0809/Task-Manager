package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.classes.ChangePasswordRequest
import com.SKO.Taskmanager.classes.DeletePhotoResponse
import com.SKO.Taskmanager.classes.EditProfileRequest
import com.SKO.Taskmanager.responses.EditProfileResponse
import com.SKO.Taskmanager.responses.LoginUser
import com.SKO.Taskmanager.responses.ProfilePhotoResponse
import com.SKO.Taskmanager.responses.RegisterUser
import com.SKO.Taskmanager.responses.SendOtpRequest
import com.SKO.Taskmanager.responses.ResetPassword
import com.SKO.Taskmanager.responses.StatusResponse
import com.SKO.Taskmanager.responses.TokenResponse
import com.SKO.Taskmanager.responses.UserDataClass
import com.SKO.Taskmanager.responses.VerifyEmailRequest
import com.SKO.Taskmanager.responses.VerifyEmailResponse
import com.SKO.Taskmanager.responses.VerifyOtpRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("auth/refresh")
    fun refreshToken(@Header("Authorization") refreshToken: String): Call<TokenResponse>

    @POST("api/User/login")
    fun login(@Body loginUser: LoginUser): Call<TokenResponse>


    @POST("api/User/register")
    fun registerUser(@Body registerUser: RegisterUser): Call<StatusResponse>

//    @POST("api/User/emailExists")
//    fun verifyEmail(@Body verifyEmailRequest: VerifyEmailRequest) : Call<VerifyEmailResponse>

    @GET("api/User/emailExists")
    fun verifyEmail(@Query("email") email: String): Call<VerifyEmailResponse>

    @POST("api/Email/sendOtp")
    fun sendOtp(@Body sendOtpRequest: SendOtpRequest) : Call<StatusResponse>

    @POST("api/Otp/validate-otp")
    fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest) : Call<String>

    @POST("api/User/forgot-password")
    fun resetPassword(@Body resetPassword : ResetPassword) : Call<StatusResponse>

//    @GET("api/UserProfile/profile")
//    fun getUserData(@Header("Authorization") jwtToken: String): Call<UserDataClass>

    @GET("api/UserProfile/profile")
    fun getUserData(): Call<UserDataClass>
    @Multipart
    @POST("api/UserProfile/profile-photo")
    fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part
    ): Call<ProfilePhotoResponse>

    @FormUrlEncoded
    @POST("update")
    fun updateStudent(@FieldMap map: Map<String, String>): Call<StatusResponse>
    @PUT("api/userprofile/profile")
    fun editProfile(@Body editProfileRequest: EditProfileRequest) : Call<EditProfileResponse>
    @POST("api/UserProfile/change-password")
    fun changePassword(@Body changePasswordRequest: ChangePasswordRequest) : Call<EditProfileResponse>

    @FormUrlEncoded
    @GET("delete")
    fun deleteStudent(@Query("id") id: String): Call<StatusResponse>
    @DELETE("api/UserProfile/profile-photo")
    fun deleteProfilePhoto(): Call<DeletePhotoResponse>


//    @GET("getStudent")
//    fun getStudent(@Query("id") id: String): Call<StudentResponse>
//
//    @GET("getAll")
//    fun getAllStudent(): Call<StudentListResponse>
}