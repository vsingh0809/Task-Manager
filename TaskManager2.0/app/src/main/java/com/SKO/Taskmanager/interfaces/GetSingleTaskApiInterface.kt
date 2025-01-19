package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.ApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GetSingleTaskApiInterface {

    @GET("api/Tasks/{taskId}")
    fun getByTaskId(@Path("taskId") taskId: Int): Call<ApiResponse>

}