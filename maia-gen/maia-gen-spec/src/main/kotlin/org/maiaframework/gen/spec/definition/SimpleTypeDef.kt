package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


abstract class SimpleTypeDef internal constructor(
    val fqcn: Fqcn,
    val superTypeFqcn: Fqcn,
    val simpleTypeUnderlyingFieldType: FieldType,
    private val isProvided: Boolean
) {


    val uqcn = this.fqcn.uqcn


    val classDef: ClassDef


    val isNotProvided = this.isProvided == false


    init {

        val superclassDef = initSuperClassDef(superTypeFqcn)
        this.classDef = aClassDef(fqcn).withSuperclass(superclassDef).build()

    }


    private fun initSuperClassDef(
        superTypeFqcn: Fqcn
    ): ClassDef {

        val superclassParameterizedType = ParameterizedType(superTypeFqcn, simpleTypeUnderlyingFieldType, ParameterizedType(fqcn, simpleTypeUnderlyingFieldType))
        val valueFieldDef = aClassField("value", simpleTypeUnderlyingFieldType).build()
        val superclassDef = aClassDef(superclassParameterizedType).withFieldDefsNotInherited(listOf(valueFieldDef)).build()
        return superclassDef

    }


}
