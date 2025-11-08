package org.maiaframework.common.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object LocalDateExtensions {


    fun LocalDate.toInstant(zone: ZoneId = ZoneId.of("UTC")): Instant {

        return this.atStartOfDay(zone).toInstant()

    }


}
