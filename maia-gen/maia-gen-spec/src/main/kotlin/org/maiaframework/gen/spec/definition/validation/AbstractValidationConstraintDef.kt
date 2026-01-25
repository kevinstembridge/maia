package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.lang.AnnotationDef

abstract class AbstractValidationConstraintDef protected constructor(
    val associatedAnnotationDef: AnnotationDef
) : Comparable<AbstractValidationConstraintDef> {


    override fun compareTo(other: AbstractValidationConstraintDef): Int {

        val thisUqcn = associatedAnnotationDef.fqcn.uqcn
        val thatAnnotationUqcn = other.associatedAnnotationDef.fqcn.uqcn
        return thisUqcn.compareTo(thatAnnotationUqcn)

    }


}
