package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


class TimelineBlotterDef(
    val entityDef: EntityDef,
    val joinDefs: List<TimelineBlotterJoinDef>
) {


    val historyEntityDef: EntityDef = entityDef.historyEntityDef
        ?: error("Entity '${entityDef.entityBaseName}' must have recordVersionHistory = true for a timelineBlotter")


    val packageName: PackageName = entityDef.packageName


    val timelineBlotterBaseName: String = "${entityDef.entityBaseName.value}TimelineBlotter"


    val historyTableSchemaAndTable: String = historyEntityDef.schemaAndTableName


    val entityHistoryColumns: List<EntityFieldDef> = historyEntityDef.allEntityFieldsSorted.filter { fieldDef ->
        val name = fieldDef.classFieldDef.classFieldName.value
        name !in setOf("id", "version", "createdTimestampUtc", "changeType", "lastModifiedTimestampUtc") &&
            fieldDef.classFieldDef.fieldType !is ForeignKeyFieldType
    }


    val rowDtoUqcn = "${timelineBlotterBaseName}RowDto"


    val rowMapperUqcn = "${timelineBlotterBaseName}RowDtoRowMapper"


    val metaUqcn = "${timelineBlotterBaseName}RowDtoMeta"


    val daoUqcn = "${timelineBlotterBaseName}RowDtoDao"


    val repoUqcn = "${timelineBlotterBaseName}RowDtoRepo"


    val searchServiceUqcn = "${timelineBlotterBaseName}RowDtoSearchService"


    val endpointUqcn = "${timelineBlotterBaseName}SearchEndpoint"


    val rowDtoFqcn = packageName.uqcn(rowDtoUqcn)


    val rowDtoClassDef = aClassDef(packageName.uqcn(rowDtoUqcn))
        .ofType(ClassType.DATA_CLASS)
        .build()


    val rowMapperClassDef = aClassDef(packageName.uqcn(rowMapperUqcn))
        .withInterface(ParameterizedType(Fqcns.MAIA_JDBC_ROW_MAPPER, ParameterizedType(rowDtoFqcn)))
        .build()


    val metaClassDef = aClassDef(packageName.uqcn(metaUqcn))
        .ofType(ClassType.OBJECT)
        .build()


    val daoClassDef = aClassDef(packageName.uqcn(daoUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val repoClassDef = aClassDef(packageName.uqcn(repoUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()


    val searchServiceClassDef = aClassDef(packageName.uqcn(searchServiceUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
        .build()


    val endpointClassDef = aClassDef(packageName.uqcn(endpointUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    private val entityKebab = entityDef.entityBaseName.toKebabCase()


    val searchEndpointPath = "/api/${entityKebab}/{entityId}/timeline/search"


    val countEndpointPath = "/api/${entityKebab}/{entityId}/timeline/count"


    val routePath = "${entityKebab}/timeline"


    val pageTitle = "${entityDef.entityBaseName.value} Timeline"


    val blotterComponentNames = AngularComponentNames(packageName, timelineBlotterBaseName)


    val blotterPageComponentNames = AngularComponentNames(packageName, "${timelineBlotterBaseName}Page")


    val datasourceClassName = "${timelineBlotterBaseName}AgGridDatasource"


    val serviceClassName = "${timelineBlotterBaseName}Service"


    val tsRowDtoClassName = "${timelineBlotterBaseName}RowDto"


    val searchEndpointUrlForTypescript = $$"/api/$${entityKebab}/${this.entityId}/timeline/search"


}
