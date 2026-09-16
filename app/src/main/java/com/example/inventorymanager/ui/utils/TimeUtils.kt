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

    /**
     * Checks if a timestamp is within the current week (starting Sunday).
     * Reset happens on Sunday 00:00:00.
     */
    fun isWithinCurrentWeek(timestamp: Timestamp?): Boolean {
        if (timestamp == null) return false
        
        val calendar = Calendar.getInstance()
        
        // Go to start of current day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Go back to the most recent Sunday
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        val startOfWeekMillis = calendar.timeInMillis
        return timestamp.toDate().time >= startOfWeekMillis
    }

    /**
     * Checks if a timestamp represents the current calendar day.
     * Handles null by returning true (assumes local pending write).
     */
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
}
