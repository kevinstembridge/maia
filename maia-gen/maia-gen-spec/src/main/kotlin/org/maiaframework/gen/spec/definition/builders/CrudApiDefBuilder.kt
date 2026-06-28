package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.CrudApiDef
import org.maiaframework.gen.spec.definition.RequestDtoDef

@MaiaDslMarker
class CrudApiDefBuilder {


    private var contextDtoDef: RequestDtoDef? = null


    private var authorityDef: AuthorityDef? = null


    fun authority(authorityDef: AuthorityDef) {

        this.authorityDef = authorityDef

    }

    fun contextDto(contextDtoDef: RequestDtoDef) {

        this.contextDtoDef = contextDtoDef

    }


    fun build(defaultAuthority: AuthorityDef?): CrudApiDef {

        val authorityToUse = this.authorityDef ?: defaultAuthority

        return CrudApiDef(
            authorityToUse,
            this.contextDtoDef
        )

    }


}
