package com.example.logiceditor.activity

import java.text.SimpleDateFormat
import java.util.*

object CurrentDate {
    fun getCurrentDate(): String {
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(currentDateTime)
    }
}