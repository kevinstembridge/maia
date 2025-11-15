package org.maiaframework.job

import org.maiaframework.common.ExceptionUtil
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.domain.DomainId
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JobExecutionRepo(private val jobExecutionDao: JobExecutionDao) {


    fun newJobExecution(
        jobInstanceId: DomainId,
        jobName: JobName,
        invokedBy: String
    ) {

        val now = Instant.now()

        val entity = JobExecutionEntity(
                null,
                now,
                null,
                null,
                jobInstanceId,
                invokedBy,
                jobName,
                now,
                emptyMap(),
                null,
                now)

        this.jobExecutionDao.insert(entity)

    }


    fun jobFailed(jobInstanceId: DomainId, jobMetrics: JobMetrics, e: Exception) {

        updateJobExecution(jobInstanceId, JobCompletionStatus.FAILED, jobMetrics, e)

    }


    fun jobCompleted(jobInstanceId: DomainId, jobMetrics: JobMetrics) {

        updateJobExecution(jobInstanceId, JobCompletionStatus.SUCCESS, jobMetrics)

    }


    private fun updateJobExecution(
        jobInstanceId: DomainId,
        completionStatus: JobCompletionStatus,
        jobMetrics: JobMetrics,
        e: Exception? = null
    ) {

        val builder = JobExecutionEntityUpdater.forPrimaryKey(jobInstanceId) {
            completionStatus(completionStatus)
            endTimestampUtc(Instant.now())
            metrics(jobMetrics.metricsReport())
        }

        e?.let {
            builder.errorMessage(it.message?.take(10_000))
            builder.stackTrace(ExceptionUtil.stackTrace(it).take(10_000))
        }

        val updater = builder.build()

        this.jobExecutionDao.setFields(updater)

    }


    fun recentFailedExecutions(jobName: JobName): List<JobExecutionEntity> {

        val filters = JobExecutionEntityFilters()
        val filter = filters.and(
                filters.jobName eq jobName,
                filters.completionStatus eq JobCompletionStatus.FAILED
        )

        val sort = Sort.by(Sort.Order.desc(JobExecutionEntityMeta.endTimestampUtc))
        val pageRequest = PageRequest.of(0, 10, sort)
        return this.jobExecutionDao.findAllBy(filter, pageRequest).toList()

    }


    fun findStacktraceForJob(jobExecutionId: DomainId): String? {

        return this.jobExecutionDao.findByPrimaryKey(jobExecutionId).stackTrace

    }


    fun findJobExecutionDetail(jobExecutionId: DomainId): JobExecutionEntity? {

        return this.jobExecutionDao.findByPrimaryKeyOrNull(jobExecutionId)

    }


}
