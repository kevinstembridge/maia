package org.maiaframework.common.time

import org.maiaframework.common.time.LocalDateExtensions.toInstant
import java.time.LocalDate


class LocalDateRange(val fromDate: LocalDate, val toDate: LocalDate) {


    init {

        if (this.fromDate.isAfter(this.toDate)) {
            throw IllegalStateException("fromDate [$fromDate] cannot be after toDate [$toDate]")
        }

    }


    fun toInstantRange(): org.maiaframework.common.time.InstantRange {

        val fromTimestamp = this.fromDate.toInstant()
        val toTimestamp = this.toDate.toInstant()

        return _root_ide_package_.org.maiaframework.common.time.InstantRange(fromTimestamp, toTimestamp)

    }


}
