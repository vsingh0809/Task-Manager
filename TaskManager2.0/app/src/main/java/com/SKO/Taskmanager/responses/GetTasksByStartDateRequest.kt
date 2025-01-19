package com.SKO.Taskmanager.responses

import java.time.LocalDate

data class GetTasksByStartDateRequest (
    var date : LocalDate,
    var userId : Int
)