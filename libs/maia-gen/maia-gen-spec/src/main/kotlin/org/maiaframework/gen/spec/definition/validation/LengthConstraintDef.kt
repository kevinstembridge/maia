package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.AnnotationDef


class LengthConstraintDef private constructor(
        val min: Long?,
        val max: Long?,
        attributes: Map<String, String>
) : AbstractValidationConstraintDef(
        AnnotationDef(
            Fqcns.VALIDATOR_CONSTRAINT_LENGTH,
            null,
            attributes
        )
) {


    companion object {

        fun of(min: Long? = null, max: Long? = null): LengthConstraintDef {

            val attributes = mutableMapOf<String, String>()

            min?.let { attributes.put("min", it.toString()) }
            max?.let { attributes.put("max", it.toString()) }

            return LengthConstraintDef(min, max, attributes)
        }

    }


}
