package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.validation.AbstractValidationConstraintDef

data class RequestDtoFieldDef(
    val classFieldDef: ClassFieldDef,
    val databaseIndexDef: DatabaseIndexDef?,
) : Comparable<RequestDtoFieldDef> {


    override fun compareTo(other: RequestDtoFieldDef): Int {

        return this.classFieldDef.compareTo(other.classFieldDef)

    }


    fun copyWith(validationConstraints: MutableSet<AbstractValidationConstraintDef>): RequestDtoFieldDef {

        return copy(classFieldDef = classFieldDef.copyWith(validationConstraints))

    }


}
