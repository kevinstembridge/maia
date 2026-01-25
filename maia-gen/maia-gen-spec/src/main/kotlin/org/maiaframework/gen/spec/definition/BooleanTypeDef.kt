package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn


class BooleanTypeDef(
    fqcn: Fqcn,
    provided: Boolean
) : SimpleTypeDef(
    fqcn,
    Fqcns.BOOLEAN_TYPE,
    FieldTypes.boolean,
    provided
)
