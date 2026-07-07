package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.DataSourceType
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


sealed class BlotterSourceDef(
    val rootEntityDef: EntityDef?
) {


    abstract val withGeneratedFindAllFunction: WithGeneratedFindAllFunction


    abstract val withGeneratedEndpoint: WithGeneratedEndpoint


    abstract val dataSourceType: DataSourceType


    abstract val rowIdField: ClassFieldDef


    val deleteDialogComponentNames = this.rootEntityDef?.deleteDialogComponentNames
        ?: throw IllegalStateException("BlotterSourceDef does not have a root Entity")


    val checkForeignKeyReferencesDialogComponentNames = this.rootEntityDef?.checkForeignKeyReferencesDialogComponentNames
        ?: throw IllegalStateException("BlotterSourceDef does not have a root Entity")


    abstract fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef


}


class BlotterSearchableDtoSourceDef(
    val searchableDtoDef: SearchableDtoDef
) : BlotterSourceDef(searchableDtoDef.dtoRootEntityDef) {


    override val withGeneratedFindAllFunction = this.searchableDtoDef.withGeneratedFindAllFunction


    override val withGeneratedEndpoint = this.searchableDtoDef.withGeneratedEndpoint


    override val dataSourceType: DataSourceType = DataSourceType.DATABASE


    override val rowIdField: ClassFieldDef = this.searchableDtoDef.idField


    override fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef {

        return this.searchableDtoDef.findSearchableDtoFieldByName(fieldPath.head()).classFieldDef

    }

}


class BlotterEsDocSourceDef(
    val esDocDef: EsDocDef
) : BlotterSourceDef(esDocDef.entityDef) {


    override val withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE


    override val withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.FALSE


    override val dataSourceType: DataSourceType = DataSourceType.ELASTIC_SEARCH


    override val rowIdField: ClassFieldDef = esDocDef.idField


    override fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef {

        return esDocDef.findFieldByPath(fieldPath)

    }

}
