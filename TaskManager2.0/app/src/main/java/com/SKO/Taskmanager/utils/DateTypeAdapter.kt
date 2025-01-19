package com.SKO.Taskmanager.utils

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date

class DateTypeAdapter : JsonDeserializer<Date> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        val dateString = json.asString
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return format.parse(dateString) ?: Date() // Default to current date if parsing fails
    }
}