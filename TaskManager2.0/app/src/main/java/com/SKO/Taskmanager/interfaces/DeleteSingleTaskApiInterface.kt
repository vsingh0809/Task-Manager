package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.StatusResponse
import retrofit2.Call
import retrofit2.http.PUT
import retrofit2.http.Path

interface DeleteSingleTaskApiInterface {

    @PUT("api/Tasks/soft-delete/{taskId}")
    fun deleteTask(@Path("taskId") taskId: Int) : Call<StatusResponse>
}