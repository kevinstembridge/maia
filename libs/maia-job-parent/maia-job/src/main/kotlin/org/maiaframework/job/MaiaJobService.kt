package org.maiaframework.job

import org.maiaframework.common.util.NamedThreadFactory
import org.maiaframework.metrics.JobMetrics
import org.maiaframework.domain.DomainId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.util.concurrent.Executors

@Component
class MaiaJobService(
    private val maiaJobRegistry: MaiaJobRegistry,
    private val jobExecutionRepo: JobExecutionRepo
) {


    private val logger = LoggerFactory.getLogger(MaiaJobService::class.java)

    private val executor = Executors.newCachedThreadPool(NamedThreadFactory("maia-job"))

    private val runningJobs: MultiValueMap<JobName, RunningJob> = LinkedMultiValueMap()


    fun getAllJobsCurrentState(): List<JobStateResponseDto> {

        val allJobDescriptions = this.maiaJobRegistry.getAllJobDescriptions()

        return allJobDescriptions.map { jobDescription ->

            val runningJobStateDtos = getRunningJobsState(jobDescription.jobName)
            JobStateResponseDto(
                    jobDescription.description,
                    jobDescription.jobName,
                    runningJobStateDtos)

        }

    }


    private fun getRunningJobsState(jobName: JobName): List<RunningJobStateResponseDto> {

        val runningJobs: List<RunningJob> = this.runningJobs[jobName] ?: emptyList()

        return runningJobs.map {
            RunningJobStateResponseDto(
                    it.id,
                    it.invokedBy,
                    it.jobName,
                    it.jobMetrics.metricsReport(),
                    it.startTimestampUtc
            )
        }

    }


    fun runJob(jobName: JobName, username: String): JobExecutionSummaryResponseDto {

        val jobInstanceId = DomainId.newId()
        val startTimestampUtc = Instant.now()
        runJobAsync(jobName, jobInstanceId, username, startTimestampUtc)
        return JobExecutionSummaryResponseDto(
                null,
                null,
                jobInstanceId,
                jobName,
                startTimestampUtc,
        )

    }


    private fun runJobAsync(
            jobName: JobName,
            jobInstanceId: DomainId,
            username: String,
            startTimestampUtc: Instant
    ) {

        this.executor.execute {

            logger.info("BEGIN: runJob('$jobName') with instanceId $jobInstanceId, invoked by $username")

            val jobMetrics = JobMetrics(jobName.value)

            initRunningJob(jobName, username, jobMetrics, jobInstanceId, startTimestampUtc)

            try {

                jobMetrics.timeInstanceOfJob {

                    val job = this.maiaJobRegistry.getJob(jobName)
                    job.executeJob(jobMetrics)

                }

                logger.info("Job completed ($jobName:$jobInstanceId)")

                this.jobExecutionRepo.jobCompleted(jobInstanceId, jobMetrics)

            } catch (e: Exception) {

                logger.error("Job $jobName with instanceId $jobInstanceId failed.", e)
                jobExecutionRepo.jobFailed(jobInstanceId, jobMetrics, e)

            } finally {

                val jobs = this.runningJobs[jobName]
                jobs?.removeIf { it.id == jobInstanceId }

                logger.info("job metrics for job $jobName with instanceId $jobInstanceId:\n${jobMetrics.getMetricsReportAsJson()}")

            }

        }

    }


    private fun initRunningJob(
        jobName: JobName,
        username: String,
        jobMetrics: JobMetrics,
        jobExecutionId: DomainId,
        startTimestampUtc: Instant
    ) {

        val runningJob = RunningJob(jobExecutionId, jobName, startTimestampUtc, username, jobMetrics)
        this.runningJobs[jobName] = runningJob
        this.jobExecutionRepo.newJobExecution(jobExecutionId, jobName, username)

    }


    fun getRecentFailures(jobName: JobName): List<JobExecutionSummaryResponseDto> {

        return jobExecutionRepo.recentFailedExecutions(jobName).map { entity ->

            JobExecutionSummaryResponseDto(
                    entity.endTimestampUtc,
                    entity.errorMessage,
                    entity.id,
                    jobName,
                    entity.startTimestampUtc)

        }

    }


    fun getJobExecutionDetailDto(jobExecutionId: DomainId): JobExecutionDetailResponseDto? {

        return getJobExecutionEntity(jobExecutionId)?.let {
            JobExecutionDetailResponseDto(
                    it.endTimestampUtc,
                    it.errorMessage,
                    it.id,
                    it.jobName,
                    it.stackTrace,
                    it.startTimestampUtc)
        }

    }


    fun getJobExecutionStacktrace(jobExecutionId: DomainId): String? {

        return getJobExecutionEntity(jobExecutionId)?.stackTrace

    }


    fun getJobExecutionEntity(jobExecutionId: DomainId): JobExecutionEntity? {

        return this.jobExecutionRepo.findJobExecutionDetail(jobExecutionId)

    }


}
