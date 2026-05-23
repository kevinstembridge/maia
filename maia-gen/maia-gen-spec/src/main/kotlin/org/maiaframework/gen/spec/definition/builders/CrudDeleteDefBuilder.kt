package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.CrudDeleteOperationDef


@MaiaDslMarker
class CrudDeleteDefBuilder {


    private var authority: AuthorityDef? = null


    private var crudApiDefBuilder: CrudApiDefBuilder? = null


    fun authority(authority: AuthorityDef) {

        this.authority = authority

    }


    fun api(
        init: CrudApiDefBuilder.() -> Unit
    ) {

        val builder = CrudApiDefBuilder()
        builder.init()
        this.crudApiDefBuilder = builder

    }


    fun build(defaultAuthority: AuthorityDef?): CrudDeleteOperationDef {

        val authorityToUse = this.authority ?: defaultAuthority

        val crudApiDef = this.crudApiDefBuilder?.build(authorityToUse)

        return CrudDeleteOperationDef(
            authorityToUse,
            crudApiDef
        )

    }


}
