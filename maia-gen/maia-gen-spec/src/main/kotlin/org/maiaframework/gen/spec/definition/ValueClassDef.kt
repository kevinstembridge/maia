package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.Uqcn


abstract class ValueClassDef(
    val fqcn: Fqcn,
    val underlyingFieldType: FieldType,
    private val isProvided: Boolean
) {


    val classDef: ClassDef = aClassDef(fqcn).build()


    val valueClassFieldType: FieldType = FieldTypes.byFqcn(fqcn)


    val uqcn: Uqcn = classDef.uqcn


    val isNotProvided: Boolean = this.isProvided == false


}
