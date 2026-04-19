package org.maiaframework.showcase.jobs

import org.maiaframework.job.JobName
import org.maiaframework.job.MaiaJob
import org.maiaframework.metrics.JobMetrics
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ShowcaseSampleJob : MaiaJob {

    private val log = LoggerFactory.getLogger(javaClass)

    override val jobName = JobName("showcase-sample")

    override val description = "Sample job demonstrating the maia-job framework"

    override fun executeJob(jm: JobMetrics) {
        log.info("ShowcaseSampleJob executing")
        jm.getOrCreateCounter("itemCount").inc(1L)
    }

}
