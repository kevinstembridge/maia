package org.maiaframework.job

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.metrics.JobMetrics
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MaiaJobServiceAbandonTest {


    private val jobName = JobName("test-job")

    private val registry = mockk<MaiaJobRegistry>(relaxed = true)

    private val repo = mockk<JobExecutionRepo>(relaxed = true)

    private val service = MaiaJobService(registry, repo)


    @Test
    fun `returns NOT_FOUND when execution does not exist`() {

        val id = DomainId.newId()
        every { repo.findJobExecutionDetail(id) } returns null

        assertThat(service.abandonJobExecution(id)).isEqualTo(AbandonJobExecutionResult.NOT_FOUND)
        verify(exactly = 0) { repo.jobAbandoned(any()) }

    }


    @Test
    fun `returns NOT_RUNNING and leaves execution untouched unless status is RUNNING`() {

        JobExecutionStatus.entries.filter { it != JobExecutionStatus.RUNNING }.forEach { status ->

            val id = DomainId.newId()
            every { repo.findJobExecutionDetail(id) } returns entity(id, status)

            assertThat(service.abandonJobExecution(id)).isEqualTo(AbandonJobExecutionResult.NOT_RUNNING)
            verify(exactly = 0) { repo.jobAbandoned(id) }

        }

    }


    @Test
    fun `abandons a RUNNING execution that is not running in this process`() {

        val id = DomainId.newId()
        every { repo.findJobExecutionDetail(id) } returns entity(id, JobExecutionStatus.RUNNING)

        assertThat(service.abandonJobExecution(id)).isEqualTo(AbandonJobExecutionResult.ABANDONED)
        verify(exactly = 1) { repo.jobAbandoned(id) }

    }


    @Test
    fun `returns STILL_RUNNING when the execution is live in this process`() {

        val started = CountDownLatch(1)
        val release = CountDownLatch(1)

        val job = object : MaiaJob {
            override val jobName = this@MaiaJobServiceAbandonTest.jobName
            override val description: String? = null
            override fun executeJob(jm: JobMetrics) {
                started.countDown()
                release.await(10, TimeUnit.SECONDS)
            }
        }
        every { registry.getJob(jobName) } returns job

        val id = service.runJob(jobName, "tester").jobExecutionId
        try {

            assertThat(started.await(10, TimeUnit.SECONDS)).isTrue()
            every { repo.findJobExecutionDetail(id) } returns entity(id, JobExecutionStatus.RUNNING)

            assertThat(service.abandonJobExecution(id)).isEqualTo(AbandonJobExecutionResult.STILL_RUNNING)
            verify(exactly = 0) { repo.jobAbandoned(any()) }

        } finally {
            release.countDown()
        }

    }


    private fun entity(id: DomainId, status: JobExecutionStatus): JobExecutionEntity {

        val now = Instant.now()
        return JobExecutionEntity(now, null, null, id, "tester", jobName, now, emptyMap(), null, now, status)

    }


}
