package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.AnnotationDef
import org.maiaframework.gen.spec.definition.lang.Fqcn


class EnumConstraintDef(enumFqcn: Fqcn) : AbstractValidationConstraintDef(AnnotationDef(
        Fqcns.VALIDATOR_CONSTRAINT_ENUM,
        null,
        mapOf("enumClass" to "$enumFqcn::class")))
