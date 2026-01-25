package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.AnnotationDef


class MaxConstraintDef private constructor(val maxValue: Long) : AbstractValidationConstraintDef(AnnotationDef(
        Fqcns.VALIDATOR_CONSTRAINT_MAX,
        { maxValue.toString() },
        emptyMap())) {


    companion object {

        fun of(maxValue: Long): MaxConstraintDef {
            return MaxConstraintDef(maxValue)
        }

    }


}
