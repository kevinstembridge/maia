package org.maiaframework.job

import org.maiaframework.webapp.domain.auth.CurrentUserHolder
import org.maiaframework.domain.DomainId
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${maia.jobs.api.base-path}")
class MaiaJobEndpoint(private val jobService: MaiaJobService) {


    @GetMapping("/jobs/current_state", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun getCurrentJobsState(): List<JobStateResponseDto> {

        return this.jobService.getAllJobsCurrentState()

    }


    @GetMapping("/job/recently_failed/{jobName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun getRecentlyFailedExecutions(
        @PathVariable jobName: String
    ): List<JobExecutionSummaryResponseDto> {

        return this.jobService.getRecentFailures(JobName(jobName))

    }


    @GetMapping("/job/execution_detail/{jobExecutionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun getExecutionDetail(
        @PathVariable jobExecutionId: String
    ): JobExecutionDetailResponseDto? {

        return this.jobService.getJobExecutionDetailDto(DomainId(jobExecutionId))

    }


    @GetMapping("/job/execution_stacktrace/{jobExecutionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun getStacktrace(
        @PathVariable jobExecutionId: String
    ): Map<String, String>? {

        val stacktrace = this.jobService.getJobExecutionStacktrace(DomainId(jobExecutionId))

        return stacktrace?.let {
            mapOf("stacktrace" to stacktrace)
        }

    }


    @PostMapping("/job/run/{jobName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasAuthority('SYS__OPS')")
    fun runJob(
        @PathVariable jobName: String
    ): JobExecutionSummaryResponseDto {

        val username = CurrentUserHolder.currentUsernameOrNull ?: "unknown"
        return this.jobService.runJob(JobName(jobName), username)

    }


}
