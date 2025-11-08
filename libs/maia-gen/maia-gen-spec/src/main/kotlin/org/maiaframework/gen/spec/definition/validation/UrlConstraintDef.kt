package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.AnnotationDefs

class UrlConstraintDef private constructor() : AbstractValidationConstraintDef(AnnotationDefs.VALIDATION_CONSTRAINT_URL) {


    companion object {

        val INSTANCE = UrlConstraintDef()

    }


}
