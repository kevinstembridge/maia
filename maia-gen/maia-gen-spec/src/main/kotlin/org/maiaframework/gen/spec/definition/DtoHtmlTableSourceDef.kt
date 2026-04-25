package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


sealed class DtoHtmlTableSourceDef {


    abstract val withGeneratedFindAllFunction: WithGeneratedFindAllFunction


    abstract val withGeneratedEndpoint: WithGeneratedEndpoint


    abstract val dataSourceType: DataSourceType


    abstract fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef


}


class DtoHtmlTableSearchableDtoSourceDef(
    val searchableDtoDef: SearchableDtoDef
) : DtoHtmlTableSourceDef() {


    override val withGeneratedFindAllFunction = this.searchableDtoDef.withGeneratedFindAllFunction


    override val withGeneratedEndpoint = this.searchableDtoDef.withGeneratedEndpoint


    override val dataSourceType: DataSourceType = DataSourceType.DATABASE


    override fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef {

        return this.searchableDtoDef.findSearchableDtoFieldByName(fieldPath.head()).classFieldDef

    }

}


class DtoHtmlTableEsDocSourceDef(
    val esDocDef: EsDocDef
) : DtoHtmlTableSourceDef() {


    override val withGeneratedFindAllFunction: WithGeneratedFindAllFunction = WithGeneratedFindAllFunction.FALSE


    override val withGeneratedEndpoint: WithGeneratedEndpoint = WithGeneratedEndpoint.FALSE


    override val dataSourceType: DataSourceType = DataSourceType.ELASTIC_SEARCH


    override fun findFieldByPath(fieldPath: FieldPath): ClassFieldDef {

        return esDocDef.findFieldByPath(fieldPath)

    }

}
