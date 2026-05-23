package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.CrudApiDef
import org.maiaframework.gen.spec.definition.CrudApiDefs
import org.maiaframework.gen.spec.definition.CustomCrudServiceFqcn
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.lang.Fqcn


@MaiaDslMarker
class CrudApiDefsBuilder(
    private val defaultAuthority: AuthorityDef?
) {


    private var createApiDef: CrudApiDef? = null


    private var updateApiDef: CrudApiDef? = null


    private var deleteApiDef: CrudApiDef? = null


    private var customCrudServiceFqcn: CustomCrudServiceFqcn? = null


    fun create(
        authority: AuthorityDef? = null,
        contextDto: RequestDtoDef? = null,
        withEntityForm: Boolean = false
    ) {

        val authorityToUse = authority ?: this.defaultAuthority
        this.createApiDef = CrudApiDef(authorityToUse, contextDto, withEntityForm)

    }


    fun update(
        authority: AuthorityDef? = null,
        contextDto: RequestDtoDef? = null,
        withEntityForm: Boolean = false
    ) {

        val authorityToUse = authority ?: this.defaultAuthority
        this.updateApiDef = CrudApiDef(authorityToUse, contextDto, withEntityForm)

    }


    fun delete(
        authority: AuthorityDef? = null,
        contextDto: RequestDtoDef? = null
    ) {

        val authorityToUse = authority ?: this.defaultAuthority
        this.deleteApiDef = CrudApiDef(authorityToUse, contextDto, withEntityForm = false)

    }


    fun customCrudService(crudServiceFqcn: String) {

        this.customCrudServiceFqcn = CustomCrudServiceFqcn(Fqcn.valueOf(crudServiceFqcn))

    }


    fun build(superclassEntityDef: EntityDef?): CrudApiDefs {

        return CrudApiDefs(
            this.createApiDef,
            this.updateApiDef,
            this.deleteApiDef,
            superclassEntityDef?.entityCrudApiDef,
            this.customCrudServiceFqcn
        )

    }


}
