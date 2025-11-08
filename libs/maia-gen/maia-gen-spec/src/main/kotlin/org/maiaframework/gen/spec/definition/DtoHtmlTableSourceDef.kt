package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.flags.WithGeneratedFindAllFunction

class DtoHtmlTableSourceDef private constructor(
    val esDocDef: EsDocDef?,
    val searchableDtoDef: SearchableDtoDef?
) {


    val withGeneratedFindAllFunction: WithGeneratedFindAllFunction
        = this.searchableDtoDef?.withGeneratedFindAllFunction
        ?: WithGeneratedFindAllFunction.FALSE


    val withGeneratedEndpoint: WithGeneratedEndpoint
        = this.searchableDtoDef?.withGeneratedEndpoint
        ?: WithGeneratedEndpoint.FALSE


    val databaseType = searchableDtoDef?.dtoRootEntityDef?.databaseType


    companion object {


        fun of(esDocDef: EsDocDef): DtoHtmlTableSourceDef {
            return DtoHtmlTableSourceDef(esDocDef, null)
        }


        fun of(searchableDtoDef: SearchableDtoDef): DtoHtmlTableSourceDef {
            return DtoHtmlTableSourceDef( null, searchableDtoDef)
        }


    }


}
