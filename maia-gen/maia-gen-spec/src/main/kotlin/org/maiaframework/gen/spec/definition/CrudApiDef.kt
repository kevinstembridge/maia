package org.maiaframework.gen.spec.definition

class CrudApiDef(
    val authority: Authority?,
    val context: RequestDtoDef?,
    val withEntityForm: Boolean
) {

    internal val _manyToManyAssociations: MutableList<ManyToManyEntityDef> = mutableListOf()
    val manyToManyAssociations: List<ManyToManyEntityDef> get() = _manyToManyAssociations

}
