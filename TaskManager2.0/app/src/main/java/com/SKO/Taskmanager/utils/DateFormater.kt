package com.SKO.Taskmanager.utils

import java.text.SimpleDateFormat
import java.util.Locale

public  class DateFormater {

        fun formatDate(dateTime: String?): String {
            if (dateTime == null) return "N/A"

            return try {
                // Parse the input date string
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(dateTime)

                // Format the date to dd-MM-yyyy
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e: Exception) {
                // Handle any parsing/formatting errors
                "Invalid date"
            }
        }
    }