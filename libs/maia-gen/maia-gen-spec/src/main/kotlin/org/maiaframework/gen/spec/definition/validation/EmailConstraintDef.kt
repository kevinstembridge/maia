package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.AnnotationDefs

class EmailConstraintDef private constructor() : AbstractValidationConstraintDef(AnnotationDefs.VALIDATION_CONSTRAINT_EMAIL) {


    companion object {

        val INSTANCE = EmailConstraintDef()

    }


}
