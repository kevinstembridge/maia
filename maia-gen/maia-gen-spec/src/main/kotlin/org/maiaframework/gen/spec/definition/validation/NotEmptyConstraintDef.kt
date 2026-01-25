package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.AnnotationDefs

class NotEmptyConstraintDef private constructor() : AbstractValidationConstraintDef(AnnotationDefs.VALIDATION_CONSTRAINT_NOT_EMPTY) {


    companion object {

        val INSTANCE = NotEmptyConstraintDef()

    }


}
