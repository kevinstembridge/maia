package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.AnnotationDef


class MinConstraintDef private constructor(
        val minValue: Long
) : AbstractValidationConstraintDef(
        AnnotationDef(
            Fqcns.VALIDATOR_CONSTRAINT_MIN,
            { minValue.toString() },
            emptyMap()
        )
) {


    companion object {

        fun of(minValue: Long): MinConstraintDef {
            return MinConstraintDef(minValue)
        }

    }


}
