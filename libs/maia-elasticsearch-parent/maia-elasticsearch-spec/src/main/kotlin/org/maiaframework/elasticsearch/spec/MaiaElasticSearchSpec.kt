@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.elasticsearch.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused")
class MaiaElasticSearchSpec: AbstractSpec(appKey = AppKey("elasticsearch"), defaultSchemaName = SchemaName("elasticsearch")) {


    val readAuthority = authority("MAIA_ELASTICSEARCH_READ")


    val writeAuthority = authority("MAIA_ELASTICSEARCH_WRITE")


    val jobNameStringType = stringType("org.maiaframework.elasticsearch.JobName") {
        provided()
    }


    val indexBaseNameStringType = stringType("org.maiaframework.elasticsearch.index.model.EsIndexBaseName") {
        provided()
    }


    val indexVersionIntType = intType("org.maiaframework.elasticsearch.index.model.EsIndexVersion") {
        provided()
    }

    val jobExecutionStatusEnumDef = enumDef("org.maiaframework.elasticsearch.JobExecutionStatus") {
        provided()
    }


    val indexBaseNameAndVersionDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "IndexBaseNameAndVersion") {
        field("baseName", indexBaseNameStringType)
        field("version", indexVersionIntType)
    }


    val indexHealthDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "EsIndexHealth") {
        field("status", FieldTypes.string)
    }


    val managedIndexInfoDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "ManagedEsIndexInfo") {
        field("indexName", indexBaseNameAndVersionDtoDef)
        field("description", FieldTypes.string)
        field("isActiveVersion", FieldTypes.boolean)
    }


    val indexStateDtoDef = simpleResponseDto("org.maiaframework.elasticsearch.index.model", "ManagedEsIndexInfo") {
        field("indexName", FieldTypes.string)
        field("managedIndexInfo", managedIndexInfoDtoDef)
        field("health", indexHealthDtoDef)
        field("exists", FieldTypes.boolean)
    }


    val runningJobStateDtoDef = simpleResponseDto("org.maiaframework.elasticsearch", "RunningJobState") {
        field("id", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("invokedBy", FieldTypes.string)
        field("startTimestamp", FieldTypes.instant)
        field("metrics", FieldTypes.mapOfStringToAny())
    }


    val jobExecutionSummaryDtoDef = simpleResponseDto("org.maiaframework.elasticsearch", "JobExecutionSummary") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
    }


    val jobExecutionHistoryItemDtoDef = simpleResponseDto("org.maiaframework.elasticsearch", "JobExecutionHistoryItem") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("invokedBy", FieldTypes.string)
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("status", jobExecutionStatusEnumDef)
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
        field("metrics", FieldTypes.mapOfStringToAny())
    }


    val jobStateDtoDef = simpleResponseDto("org.maiaframework.elasticsearch", "JobState") {
        field("jobName", jobNameStringType)
        field("description", FieldTypes.string) {
            nullable()
        }
        field("runningJobs", fieldListOf(runningJobStateDtoDef))
        field("recentlyFailedExecutions", fieldListOf(jobExecutionSummaryDtoDef))
    }


    val jobExecutionDetailDtoDef = simpleResponseDto("org.maiaframework.elasticsearch", "JobExecutionDetail") {
        field("jobExecutionId", FieldTypes.domainId)
        field("jobName", jobNameStringType)
        field("startTimestamp", FieldTypes.instant)
        field("endTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("errorMessage", FieldTypes.string) {
            nullable()
        }
        field("stackTrace", FieldTypes.string) {
            nullable()
        }
    }


}
