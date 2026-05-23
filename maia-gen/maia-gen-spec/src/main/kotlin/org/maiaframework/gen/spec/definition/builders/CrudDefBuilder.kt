package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.CrudDef
import org.maiaframework.gen.spec.definition.CrudOperationDefs
import org.maiaframework.gen.spec.definition.CustomCrudServiceFqcn
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.flags.WithCrudListener
import org.maiaframework.gen.spec.definition.lang.Fqcn


@MaiaDslMarker
class CrudDefBuilder {


    var withCrudListener = WithCrudListener.FALSE


    private var authority: AuthorityDef? = null


    private var customCrudServiceFqcn: CustomCrudServiceFqcn? = null


    private var crudCreateDefBuilder: CrudCreateDefBuilder? = null


    private var crudUpdateDefBuilder: CrudUpdateDefBuilder? = null


    private var crudDeleteDefBuilder: CrudDeleteDefBuilder? = null


    fun build(superclassEntityDef: EntityDef?): CrudDef {

        val createOperationDef = this.crudCreateDefBuilder?.build(this.authority)
        val updateOperationDef = this.crudUpdateDefBuilder?.build(this.authority)
        val deleteOperationDef = this.crudDeleteDefBuilder?.build(this.authority)

        val crudOperationDefs = CrudOperationDefs(
            createOperationDef,
            updateOperationDef,
            deleteOperationDef,
            superclassEntityDef?.entityCrudApiDef,
            customCrudServiceFqcn
        )

        return CrudDef(
            this.withCrudListener,
            crudOperationDefs
        )

    }


    fun authority(authority: AuthorityDef) {

        this.authority = authority

    }


    fun customCrudServiceFqcn(fqcn: String) {

        this.customCrudServiceFqcn = CustomCrudServiceFqcn(Fqcn.valueOf(fqcn))

    }


    fun create(
        init: (CrudCreateDefBuilder.() -> Unit)? = null
    ) {

        val builder = CrudCreateDefBuilder()
        init?.invoke(builder)
        this.crudCreateDefBuilder = builder

    }


    fun update(
        init: (CrudUpdateDefBuilder.() -> Unit)? = null
    ) {

        val builder = CrudUpdateDefBuilder()
        init?.invoke(builder)
        this.crudUpdateDefBuilder = builder

    }


    fun delete(
        init: (CrudDeleteDefBuilder.() -> Unit)? = null
    ) {

        val builder = CrudDeleteDefBuilder()
        init?.invoke(builder)
        this.crudDeleteDefBuilder = builder

    }


}
