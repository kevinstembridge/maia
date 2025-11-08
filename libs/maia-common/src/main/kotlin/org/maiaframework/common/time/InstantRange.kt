package org.maiaframework.common.time

import java.time.Instant

class InstantRange(val fromTimestamp: Instant, val toTimestamp: Instant) {


    init {

        if (this.fromTimestamp.isAfter(this.toTimestamp)) {
            throw IllegalStateException("fromTimestamp [$fromTimestamp] cannot be after toTimestamp [$toTimestamp]")
        }

    }


}
