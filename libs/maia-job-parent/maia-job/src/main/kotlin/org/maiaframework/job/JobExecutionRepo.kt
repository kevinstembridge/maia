package org.maiaframework.job

import org.maiaframework.common.ExceptionUtil
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.search.SearchResultPage
import org.maiaframework.metrics.JobMetrics
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant

// Bounds a single query across ALL jobs (not per-job) to avoid N+1; MaiaJobService truncates to 10 per job after grouping
private const val RECENT_FAILURES_QUERY_LIMIT = 200

class JobExecutionRepo(private val jobExecutionDao: JobExecutionDao) {


    fun newJobExecution(
        jobInstanceId: DomainId,
        jobName: JobName,
        invokedBy: String
    ) {

        val now = Instant.now()

        val entity = JobExecutionEntity(
                now,
                null,
                null,
                jobInstanceId,
                invokedBy,
                jobName,
                now,
                emptyMap(),
                null,
                now,
                JobExecutionStatus.RUNNING)

        this.jobExecutionDao.insert(entity)

    }


    fun jobFailed(
        jobInstanceId: DomainId,
        jobName: JobName,
        invokedBy: String,
        startTimestamp: Instant,
        jobMetrics: JobMetrics,
        e: Exception
    ) {

        val updatedRowCount = updateJobExecution(jobInstanceId, JobExecutionStatus.FAILED, jobMetrics, e)

        if (updatedRowCount == 0) {

            // No existing row to update - most likely newJobExecution()'s insert never succeeded.
            // Insert the failure directly so it isn't lost silently.
            val now = Instant.now()

            this.jobExecutionDao.insert(
                JobExecutionEntity(
                    startTimestamp,
                    now,
                    e.message?.take(10_000),
                    jobInstanceId,
                    invokedBy,
                    jobName,
                    now,
                    jobMetrics.metricsReport(),
                    ExceptionUtil.stackTrace(e).take(10_000),
                    startTimestamp,
                    JobExecutionStatus.FAILED
                )
            )

        }

    }


    fun jobCompleted(jobInstanceId: DomainId, jobMetrics: JobMetrics) {

        updateJobExecution(jobInstanceId, JobExecutionStatus.SUCCESS, jobMetrics)

    }


    private fun updateJobExecution(
        jobInstanceId: DomainId,
        status: JobExecutionStatus,
        jobMetrics: JobMetrics,
        e: Exception? = null
    ): Int {

        val updater = JobExecutionEntityUpdater.forPrimaryKey(jobInstanceId) {
            status(status)
            endTimestamp(Instant.now())
            metrics(jobMetrics.metricsReport())

            if (e != null) {
                errorMessage(e.message?.take(10_000))
                stackTrace(ExceptionUtil.stackTrace(e).take(10_000))
            }

        }

        return this.jobExecutionDao.setFields(updater)

    }


    fun recentFailedExecutions(jobNames: Iterable<JobName>): List<JobExecutionEntity> {

        val jobNameList = jobNames.toList()

        if (jobNameList.isEmpty()) {
            return emptyList()
        }

        val filters = JobExecutionEntityFilters()
        val filter = filters.and(
                filters.jobName `in` jobNameList,
                filters.status eq JobExecutionStatus.FAILED
        )

        val sort = Sort.by(Sort.Order.desc(JobExecutionEntityMeta.endTimestamp))
        val pageRequest = PageRequest.of(0, RECENT_FAILURES_QUERY_LIMIT, sort)
        return this.jobExecutionDao.findAllBy(filter, pageRequest).toList()

    }


    fun searchExecutionHistory(
        jobName: JobName?,
        status: JobExecutionStatus?,
        from: Instant?,
        to: Instant?,
        offset: Int,
        limit: Int
    ): SearchResultPage<JobExecutionEntity> {

        val filters = JobExecutionEntityFilters()
        val conditions = mutableListOf<JobExecutionEntityFilter>()

        jobName?.let { conditions.add(filters.jobName eq it) }

        status?.let { conditions.add(filters.status eq it) }

        from?.let { conditions.add(filters.startTimestamp gte it) }
        to?.let { conditions.add(filters.startTimestamp lte it) }

        val filter = if (conditions.isEmpty()) {
            JobExecutionEntityFilters.NoopFilter()
        } else {
            filters.and(*conditions.toTypedArray())
        }

        val sort = Sort.by(Sort.Order.desc(JobExecutionEntityMeta.startTimestamp))
        val pageRequest = PageRequest.of(offset / limit, limit, sort)

        val results = this.jobExecutionDao.findAllBy(filter, pageRequest)
        val totalCount = this.jobExecutionDao.count(filter)

        return SearchResultPage(results, totalCount, offset, limit)

    }


    fun findStacktraceForJob(jobExecutionId: DomainId): String? {

        return this.jobExecutionDao.findByPrimaryKey(jobExecutionId).stackTrace

    }


    fun findJobExecutionDetail(jobExecutionId: DomainId): JobExecutionEntity? {

        return this.jobExecutionDao.findByPrimaryKeyOrNull(jobExecutionId)

    }


}
