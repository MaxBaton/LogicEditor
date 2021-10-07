package com.example.logiceditor.activity

import java.text.SimpleDateFormat
import java.util.*

object CurrentDate {
    fun getCurrentDate(): String {
        val currentDateTime = Calendar.getInstance().time
        val formater = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = formater.format(currentDateTime)
        return date
    }
}