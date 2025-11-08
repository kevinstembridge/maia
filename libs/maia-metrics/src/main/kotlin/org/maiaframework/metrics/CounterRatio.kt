package org.maiaframework.metrics

import com.codahale.metrics.Counter
import com.codahale.metrics.RatioGauge

class CounterRatio(val numerator: Counter, val denominator: Counter) : RatioGauge() {


    fun count(runnable: () -> Unit) {

        this.denominator.inc()
        runnable.invoke()
        this.numerator.inc()

    }


    fun <T> countIfNotNull(func: () -> T?): T? {

        this.denominator.inc()
        val result = func.invoke()

        if (result != null) {
            this.numerator.inc()
        }

        return result

    }


    override fun getRatio(): Ratio {

        return Ratio.of(this.numerator.count.toDouble(), this.denominator.count.toDouble())

    }


    override fun toString(): String {

        return ratio.toString()

    }


}
