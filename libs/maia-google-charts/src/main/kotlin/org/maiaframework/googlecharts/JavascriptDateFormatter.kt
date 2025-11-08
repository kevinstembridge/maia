package org.maiaframework.googlecharts

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoField

object JavascriptDateFormatter {


    fun format(instant: Instant): String {

        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))

        val year = localDateTime.get(ChronoField.YEAR)
        val monthOfYear = localDateTime.get(ChronoField.MONTH_OF_YEAR) - 1
        val dayOfMonth = localDateTime.get(ChronoField.DAY_OF_MONTH)
        val hourOfDay = localDateTime.get(ChronoField.HOUR_OF_DAY)
        val minuteOfHour = localDateTime.get(ChronoField.MINUTE_OF_HOUR)
        val secondOfMinute = localDateTime.get(ChronoField.SECOND_OF_MINUTE)
        val milliOfSecond = localDateTime.get(ChronoField.MILLI_OF_SECOND)

        return "Date($year, $monthOfYear, $dayOfMonth, $hourOfDay, $minuteOfHour, $secondOfMinute, $milliOfSecond)"

    }


}
