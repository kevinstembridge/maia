package org.maiaframework.job

import org.maiaframework.common.ExceptionUtil
import org.maiaframework.domain.DomainId
import org.maiaframework.metrics.JobMetrics
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant

private const val RECENT_FAILURES_QUERY_LIMIT = 200

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

        val updater = JobExecutionEntityUpdater.forPrimaryKey(jobInstanceId) {
            completionStatus(completionStatus)
            endTimestamp(Instant.now())
            metrics(jobMetrics.metricsReport())

            if (e != null) {
                errorMessage(e.message?.take(10_000))
                stackTrace(ExceptionUtil.stackTrace(e).take(10_000))
            }

        }

        this.jobExecutionDao.setFields(updater)

    }


    fun recentFailedExecutions(jobNames: Iterable<JobName>): List<JobExecutionEntity> {

        val jobNameList = jobNames.toList()

        if (jobNameList.isEmpty()) {
            return emptyList()
        }

        val filters = JobExecutionEntityFilters()
        val filter = filters.and(
                filters.jobName `in` jobNameList,
                filters.completionStatus eq JobCompletionStatus.FAILED
        )

        val sort = Sort.by(Sort.Order.desc(JobExecutionEntityMeta.endTimestamp))
        val pageRequest = PageRequest.of(0, RECENT_FAILURES_QUERY_LIMIT, sort)
        return this.jobExecutionDao.findAllBy(filter, pageRequest).toList()

    }


    fun findStacktraceForJob(jobExecutionId: DomainId): String? {

        return this.jobExecutionDao.findByPrimaryKey(jobExecutionId).stackTrace

    }


    fun findJobExecutionDetail(jobExecutionId: DomainId): JobExecutionEntity? {

        return this.jobExecutionDao.findByPrimaryKeyOrNull(jobExecutionId)

    }


}
