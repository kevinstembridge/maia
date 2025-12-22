package org.maiaframework.csv.diff

import java.util.concurrent.atomic.AtomicInteger

class DiffSummary {


    private val differenceCount = AtomicInteger()


    private val totalCompared = AtomicInteger()


    val differencesFound: Int
        get() = differenceCount.get()


    fun incrementDifferences(): Int {

        return differenceCount.incrementAndGet()

    }


    fun getTotalCompared(): Int {

        return totalCompared.get()

    }


    fun incrementTotalCompared(): Int {

        return totalCompared.incrementAndGet()

    }


}
