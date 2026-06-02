package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


class EntityHistoryBlotterDef(val entityDef: EntityDef) {


    val historyEntityDef: EntityDef = entityDef.historyEntityDef!!


    val packageName: PackageName = entityDef.packageName


    // e.g. "HistorySampleHistoryBlotter"
    val historyBlotterBaseName: String = "${entityDef.entityBaseName.value}HistoryBlotter"


    // e.g. "maia.history_sample_history"
    val historyTableSchemaAndTable: String = historyEntityDef.schemaAndTableName


    // Columns: version, changeType, non-FK non-id entity data fields, lastModifiedTimestampUtc
    // Excludes: id (in URL), createdBy/lastModifiedBy (raw FK IDs), createdTimestampUtc
    val blotterColumns: List<EntityFieldDef> = historyEntityDef.allEntityFieldsSorted.filter { fieldDef ->
        val name = fieldDef.classFieldDef.classFieldName.value
        val isFK = fieldDef.classFieldDef.fieldType is ForeignKeyFieldType
        val isEntityId = name == "id"
        val isCreatedTimestamp = name == "createdTimestampUtc"
        !isFK && !isEntityId && !isCreatedTimestamp
    }


    // ── Backend Kotlin names ──────────────────────────────────────────────────

    val rowDtoUqcn = "${historyBlotterBaseName}RowDto"
    val rowMapperUqcn = "${historyBlotterBaseName}RowDtoRowMapper"
    val metaUqcn = "${historyBlotterBaseName}RowDtoMeta"
    val daoUqcn = "${historyBlotterBaseName}RowDtoDao"
    val repoUqcn = "${historyBlotterBaseName}RowDtoRepo"
    val searchServiceUqcn = "${historyBlotterBaseName}RowDtoSearchService"
    val endpointUqcn = "${historyBlotterBaseName}SearchEndpoint"

    val rowDtoClassDef = aClassDef(packageName.uqcn(rowDtoUqcn))
        .ofType(ClassType.DATA_CLASS)
        .withFieldDefsNotInherited(blotterColumns.map { it.classFieldDef })
        .build()

    val rowDtoFqcn = packageName.uqcn(rowDtoUqcn)

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


    // ── Endpoint paths ────────────────────────────────────────────────────────

    private val entityKebab = entityDef.entityBaseName.toKebabCase()

    val searchEndpointPath = "/api/${entityKebab}/{entityId}/history/search"
    val countEndpointPath = "/api/${entityKebab}/{entityId}/history/count"


    // ── Frontend names ────────────────────────────────────────────────────────

    val pageTitle = "${entityDef.entityBaseName.value} History"

    // Route path WITHOUT :id — routes renderer appends /:id
    val routePath = "${entityKebab}/history"

    val blotterComponentNames = AngularComponentNames(packageName, historyBlotterBaseName)
    val blotterPageComponentNames = AngularComponentNames(packageName, "${historyBlotterBaseName}Page")

    val datasourceClassName = "${historyBlotterBaseName}AgGridDatasource"
    val serviceClassName = "${historyBlotterBaseName}Service"
    val tsRowDtoClassName = "${historyBlotterBaseName}RowDto"

    // TypeScript template literal URL with ${this.entityId} (literal TS interpolation)
    val searchEndpointUrlForTypescript = $$"/api/$${entityKebab}/${this.entityId}/history/search"

}
