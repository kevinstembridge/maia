package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.Authority
import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.CrudApiDefs
import org.maiaframework.gen.spec.definition.CrudDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.WithCrudListener


class CrudDefBuilder {


    var withCrudListener = WithCrudListener.FALSE


    private var crudApiDefsBuilder: CrudApiDefsBuilder? = null


    fun build(superclassEntityDef: EntityDef?): CrudDef {

        return CrudDef(
            this.withCrudListener,
            this.crudApiDefsBuilder?.build(superclassEntityDef) ?: CrudApiDefs.EMPTY
        )

    }


    fun apis(
        defaultAuthority: AuthorityDef? = null,
        init: CrudApiDefsBuilder.() -> Unit
    ) {

        val preAuthorizeExpression = defaultAuthority?.let { Authority(it.name) }
        val builder = CrudApiDefsBuilder(
            preAuthorizeExpression,
        )

        this.crudApiDefsBuilder = builder
        builder.init()

    }


}
