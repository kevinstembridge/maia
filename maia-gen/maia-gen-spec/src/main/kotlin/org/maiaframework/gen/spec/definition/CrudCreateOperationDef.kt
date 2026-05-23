package org.maiaframework.gen.spec.definition


class CrudCreateOperationDef(
    val authorityDef: AuthorityDef?,
    val crudApiDef: CrudApiDef?
) {


    internal val _manyToManyAssociations: MutableList<ManyToManyEntityDef> = mutableListOf()


    val manyToManyAssociations: List<ManyToManyEntityDef> get() = _manyToManyAssociations


}
