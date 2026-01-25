package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.AnnotationDefs


class NotNullConstraintDef private constructor() : AbstractValidationConstraintDef(AnnotationDefs.VALIDATION_CONSTRAINT_NOT_NULL) {


    companion object {

        val INSTANCE = NotNullConstraintDef()

    }


}
