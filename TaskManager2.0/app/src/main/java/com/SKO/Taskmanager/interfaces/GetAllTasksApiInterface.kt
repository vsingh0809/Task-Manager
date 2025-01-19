package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.GetAllTaskResponse
import retrofit2.Call
import retrofit2.http.GET

interface GetAllTasksApiInterface {

    @GET("api/Tasks/byuser")
    fun getAllTasksByToken(): Call<GetAllTaskResponse>

}