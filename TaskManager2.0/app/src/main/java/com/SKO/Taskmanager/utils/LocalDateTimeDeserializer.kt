package com.SKO.Taskmanager.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonDeserializer
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): LocalDateTime {
        val dateString = json?.asString ?: throw JsonParseException("Null or empty date string")
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return LocalDateTime.parse(dateString, formatter)
    }
}