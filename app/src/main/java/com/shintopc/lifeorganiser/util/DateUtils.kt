package com.shintopc.lifeorganiser.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object DateUtils {
    fun getRelativeTimeString(timeInMillis: Long): String {
        val now = LocalDate.now()
        val date = Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        
        return when {
            date.isEqual(now) -> "Today"
            date.isEqual(now.plusDays(1)) -> "Tomorrow"
            date.isEqual(now.minusDays(1)) -> "Yesterday"
            else -> {
                val days = ChronoUnit.DAYS.between(now, date)
                if (days in 2..7) "In $days days"
                else if (days in -7..-2) "${abs(days)} days ago"
                else DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date) // Absolute date for older/future items
            }
        }
    }
}
