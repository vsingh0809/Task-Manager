package com.SKO.Taskmanager.interfaces

import com.SKO.Taskmanager.responses.MultiselectDeleteRequest
import com.SKO.Taskmanager.responses.StatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

interface DeleteMultipleTasksInterface {

    @PUT("api/Tasks/multi-delete")
    fun deleteMultipleTasks(@Body multiselectDeleteRequest: MultiselectDeleteRequest) : Call<StatusResponse>

}