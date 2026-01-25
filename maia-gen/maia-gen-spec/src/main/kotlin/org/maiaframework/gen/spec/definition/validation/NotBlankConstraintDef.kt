package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.AnnotationDefs

class NotBlankConstraintDef private constructor() : AbstractValidationConstraintDef(AnnotationDefs.VALIDATION_CONSTRAINT_NOT_BLANK) {


    companion object {

        val INSTANCE = NotBlankConstraintDef()

    }


}
