package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.AnnotationDef


class PeriodConstraintDef private constructor() : AbstractValidationConstraintDef(
    AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_PERIOD)
) {


    companion object {

        val INSTANCE = PeriodConstraintDef()

    }


}
