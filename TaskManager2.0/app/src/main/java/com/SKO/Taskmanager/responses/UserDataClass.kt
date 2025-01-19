package com.SKO.Taskmanager.responses

import android.media.Image

data class UserDataClass(
    var userId: UInt,
    var firstName: String,
    var lastName: String,
    var email: String,
    var username: String,
    var image: String
){}
