package com.example.inventorymanager.ui.utils

import android.text.format.DateUtils
import com.google.firebase.Timestamp
import java.util.Calendar

object TimeUtils {

    fun getRelativeTime(timestamp: Timestamp?): String {
        if (timestamp == null) return "Never"
        
        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        if (time > now) return "Just now"
        
        return DateUtils.getRelativeTimeSpanString(
            time,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun isWithinCurrentWeek(timestamp: Timestamp?): Boolean {
        if (timestamp == null) return false
        
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        val startOfWeekMillis = calendar.timeInMillis
        return timestamp.toDate().time >= startOfWeekMillis
    }

    fun isToday(timestamp: Timestamp?): Boolean {
        if (timestamp == null) return true // Treat pending as today
        
        val date = timestamp.toDate()
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)
        
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_YEAR) == todayDay && 
               calendar.get(Calendar.YEAR) == todayYear
    }


    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..21 -> "Good Evening,"
            else -> "Good Night,"
        }
    }
}
