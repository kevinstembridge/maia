package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType


class EntityHistoryBlotterDef(val entityDef: EntityDef) {


    val historyEntityDef: EntityDef = entityDef.historyEntityDef!!


    val packageName: PackageName = entityDef.packageName


    val historyBlotterBaseName: String = "${entityDef.entityBaseName.value}HistoryBlotter"


    val historyTableSchemaAndTable: String = historyEntityDef.schemaAndTableName


    val isJoinEntityHistory: Boolean = entityDef.isManyToManyJoinEntity


    val blotterColumns: List<EntityFieldDef> = historyEntityDef.allEntityFieldsSorted.filter { fieldDef ->
        val name = fieldDef.classFieldDef.classFieldName.value
        val isFK = fieldDef.classFieldDef.fieldType is ForeignKeyFieldType
        val isEntityId = name == "id"
        val isVersion = name == "version"
        val isCreatedTimestamp = name == "createdTimestampUtc"

        if (isJoinEntityHistory) {
            !isEntityId && !isVersion
        } else {
            !isFK && !isEntityId && !isCreatedTimestamp
        }

    }


    val requiresJsonMapper = blotterColumns.any { col ->
        val ft = col.classFieldDef.fieldType
        ft is DataClassFieldType
        || ft is MapFieldType
        || (ft is ListFieldType && ft.parameterFieldType is DataClassFieldType)
        || (ft is SimpleResponseDtoFieldType)
    }


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


    private val entityKebab = entityDef.entityBaseName.toKebabCase()


    val searchEndpointPath = if (isJoinEntityHistory) {
        "/api/${entityKebab}/history/search"
    } else {
        "/api/${entityKebab}/{entityId}/history/search"
    }


    val countEndpointPath = if (isJoinEntityHistory) {
        "/api/${entityKebab}/history/count"
    } else {
        "/api/${entityKebab}/{entityId}/history/count"
    }


    val pageTitle = "${entityDef.entityBaseName.value} History"


    val routePath = if (isJoinEntityHistory) {
        "${entityKebab}-history"
    } else {
        "${entityKebab}/history"
    }


    val blotterComponentNames = AngularComponentNames(packageName, historyBlotterBaseName)


    val blotterPageComponentNames = AngularComponentNames(packageName, "${historyBlotterBaseName}Page")


    val datasourceClassName = "${historyBlotterBaseName}AgGridDatasource"


    val serviceClassName = "${historyBlotterBaseName}Service"


    val tsRowDtoClassName = "${historyBlotterBaseName}RowDto"


    val searchEndpointUrlForTypescript = if (isJoinEntityHistory) {
        "/api/${entityKebab}/history/search"
    } else {
        $$"/api/$${entityKebab}/${this.entityId}/history/search"
    }


}
