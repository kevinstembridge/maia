package org.maiaframework.metrics

import com.codahale.metrics.Counter
import com.codahale.metrics.RatioGauge
import com.codahale.metrics.Timer
import com.codahale.metrics.UniformReservoir
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.json.JsonMapper
import kotlin.time.Duration.Companion.nanoseconds

class JobMetrics(val jobName: String) : Comparable<JobMetrics> {

    private val childJobs = LinkedHashMap<String, JobMetrics>()
    private val timer = Timer(UniformReservoir())
    private val ratiosByName = LinkedHashMap<String, CounterRatio>()
    private val countersByName = LinkedHashMap<String, Counter>()
    private val context = mutableMapOf<String, String>()


    init {

        require(jobName.isNotBlank()) { "jobName cannot be blank" }
    }


    fun getMetricsReportAsJson(): String {

        try {
            return JSON_MAPPER.writeValueAsString(metricsReport())
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }

    }


    fun getCount(): Long {

        return this.timer.count

    }


    fun metricsReport(): Map<String, Any> {

        val report = linkedMapOf<String, Any>()
        val snapshot = this.timer.snapshot
        val count = this.timer.count
        val mean = snapshot.mean

        report["jobName"] = this.jobName
        report["jobCount"] = count
        report["context"] = this.context
        report["totalElapsedTime"] = mapOf(
            "seconds" to nanosToSeconds(count * mean),
            "formatted" to ((count * mean).nanoseconds).toString()
        )

        if (count > 1) {
            report["min"] = nanosToSeconds(snapshot.min.toDouble())
            report["median"] = nanosToSeconds(snapshot.median)
            report["95th"] = nanosToSeconds(snapshot.get95thPercentile())
            report["max"] = nanosToSeconds(snapshot.max.toDouble())
            report["mean"] = nanosToSeconds(snapshot.mean)
        }

        if (this.countersByName.isNotEmpty()) {

            val mapOfCounts = countersByName.mapValues { it.value.count }

            report["counters"] = mapOfCounts

        }

        if (this.ratiosByName.isNotEmpty()) {

            val ratiosMap = this.ratiosByName
                    .mapValues {
                        val ratioGauge = it.value
                        val map = HashMap<String, Any>()
                        map["toString"] = ratioGauge.toString()
                        map["value"] = ratioGauge.getValue()
                        map

                    }

            report["ratios"] = ratiosMap

        }

        if (this.childJobs.isNotEmpty()) {

            val childJobReports: List<Map<String, Any>> = childJobs
                    .values
                    .map { it.metricsReport() }

            report["childJobs"] = childJobReports

        }

        return report

    }


    private fun nanosToSeconds(nanos: Double): Double {

        return nanos / 1000_000_000

    }


    fun timeCountableChildJob(childName: String, countJob: () -> Int) {

        timeCountableChildJob(childName, { _ -> countJob.invoke() })

    }


    @JvmOverloads
    fun timeCountableChildJob(
            childName: String,
            countJob: (JobMetrics) -> Int,
            counterName: String = childName + "Count"
    ) {

        val childMetrics = getOrCreateChildJob(childName)
        childMetrics.timeInstanceOfJob {
            val count = countJob.invoke(childMetrics)
            childMetrics.getOrCreateCounter(counterName).inc(count.toLong())
        }

    }


    fun timeChildJob(childName: String, job: (JobMetrics) -> Unit) {

        val childMetrics = getOrCreateChildJob(childName)
        childMetrics.timeInstanceOfJob { job.invoke(childMetrics) }

    }


    fun <T> timeChildJobWithResult(childName: String, job: (JobMetrics) -> T): T {

        val childMetrics = getOrCreateChildJob(childName)
        return childMetrics.timeInstanceOfJobWithResult { job.invoke(childMetrics) }

    }


    fun timeInstanceOfJob(runnable: () -> Unit) {

        try {
            this.timer.time<Any> {
                runnable.invoke()
                null
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }


    fun <T> timeInstanceOfJobWithResult(callable: () -> T): T {

        try {
            return this.timer.time(callable)
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }


    fun getOrCreateCounterRatio(
        ratioName: String,
        denominator: Counter = Counter()
    ): CounterRatio {

        return (this.ratiosByName as java.util.Map<String, CounterRatio>).computeIfAbsent(ratioName) { k -> CounterRatio(Counter(), denominator) }

    }


    fun getCounter(counterName: String): Counter? {

        return this.countersByName[counterName]

    }


    fun getOrCreateCounter(counterName: String): Counter {

        return (this.countersByName as java.util.Map<String, Counter>).computeIfAbsent(counterName) { k -> Counter() }

    }


    fun getRatioByName(ratioName: String): RatioGauge? {

        return this.ratiosByName[ratioName]

    }


    fun getChildJobByName(jobName: String): JobMetrics? {

        return this.childJobs[jobName]

    }


    fun getOrCreateChildJob(childName: String): JobMetrics {

        return (this.childJobs as java.util.Map<String, JobMetrics>).computeIfAbsent(childName) { JobMetrics(it) }

    }


    override fun compareTo(other: JobMetrics): Int {

        return this.jobName.compareTo(other.jobName)

    }


    override fun toString(): String {

        return getMetricsReportAsJson()

    }


    fun addToContext(key: String, value: String) {

        this.context.put(key, value)

    }


    fun addAllToContext(map: Map<String, String>) {
        this.context.putAll(map)
    }


    companion object {

        private val JSON_MAPPER = JsonMapper()

    }


}
