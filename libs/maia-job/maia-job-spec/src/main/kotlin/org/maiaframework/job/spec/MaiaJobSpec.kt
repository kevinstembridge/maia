@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.job.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused")
class MaiaJobSpec: AbstractSpec(appKey = AppKey("jobs"), defaultSchemaName = SchemaName("jobs")) {


    val jobNameStringType = stringType("org.maiaframework.job.JobName") {
        provided()
    }

    val jobCompletionStatusEnumDef = enumDef("org.maiaframework.job.JobCompletionStatus") {
        provided()
    }

    val jobExecutionEntityDef = entity("org.maiaframework.job", "JobExecution") {
        tableName(name = "job_execution")
        field("jobName", jobNameStringType) {
            lengthConstraint(max = 100)
        }
        field("invokedBy", FieldTypes.string) {
            lengthConstraint(max = 100)
        }
        field("startTimestampUtc", FieldTypes.instant)
        field("endTimestampUtc", FieldTypes.instant) {
            nullable()
            modifiableBySystem()
        }
        field("completionStatus", jobCompletionStatusEnumDef) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 50)
        }
        field("metrics", mapOfStringToAny()) {
            modifiableBySystem()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 1000)
        }
        field("stackTrace", FieldTypes.string) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 10_000)
        }
        field_lastModifiedTimestampUtc()
        index {
            indexName("jobName_idx")
            withFieldAscending("jobName")
        }
    }


    val runningJobStateDtoDef = simpleResponseDto("org.maiaframework.job", "RunningJobState") {
        field("id", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("invokedBy", FieldTypes.string)
        field("startTimestampUtc", FieldTypes.instant)
        field("metrics", mapOfStringToAny())
    }


    val jobStateDtoDef = simpleResponseDto("org.maiaframework.job", "JobState") {
        field("jobName", jobNameStringType)
        field("description", FieldTypes.string) {
            nullable()
        }
        field("runningJobs", fieldListOf(runningJobStateDtoDef))
    }


    val jobExecutionDetailDtoDef = simpleResponseDto("org.maiaframework.job", "JobExecutionDetail") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("startTimestampUtc", FieldTypes.instant)
        field("endTimestampUtc", FieldTypes.instant) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
        field("stackTrace", FieldTypes.string) {
            nullable()
        }
    }


    val jobExecutionSummaryDtoDef = simpleResponseDto("org.maiaframework.job", "JobExecutionSummary") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("startTimestampUtc", FieldTypes.instant)
        field("endTimestampUtc", FieldTypes.instant) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
    }


}
