package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.Authority
import org.maiaframework.gen.spec.definition.CrudApiDef
import org.maiaframework.gen.spec.definition.CrudApiDefs
import org.maiaframework.gen.spec.definition.CustomCrudServiceFqcn
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.lang.Fqcn


class CrudApiDefsBuilder(
    private val defaultAuthority: Authority?
) {


    private var createApiDef: CrudApiDef? = null


    private var updateApiDef: CrudApiDef? = null


    private var deleteApiDef: CrudApiDef? = null


    private var customCrudServiceFqcn: CustomCrudServiceFqcn? = null


    fun create(
        authority: String? = null,
        contextDto: RequestDtoDef? = null,
        withEntityForm: Boolean = false,
    ) {

        val authorityToUse = authority?.let { Authority(it) } ?: this.defaultAuthority
        this.createApiDef = CrudApiDef(authorityToUse, contextDto, withEntityForm)

    }


    fun update(
        authority: String? = null,
        contextDto: RequestDtoDef? = null,
        withEntityForm: Boolean = false
    ) {

        val authorityToUse = authority?.let { Authority(it) } ?: this.defaultAuthority
        this.updateApiDef = CrudApiDef(authorityToUse, contextDto, withEntityForm)

    }


    fun delete(
        authority: String? = null,
        contextDto: RequestDtoDef? = null
    ) {

        val authorityToUse = authority?.let { Authority(it) } ?: this.defaultAuthority
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
