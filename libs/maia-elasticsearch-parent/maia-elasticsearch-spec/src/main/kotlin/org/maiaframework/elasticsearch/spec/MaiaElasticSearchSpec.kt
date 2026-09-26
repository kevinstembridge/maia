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


    val indexBaseNameStringType = stringType("org.maiaframework.elasticsearch.index.model.EsIndexBaseName") {
        provided()
    }


    val indexVersionIntType = intType("org.maiaframework.elasticsearch.index.model.EsIndexVersion") {
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


}
