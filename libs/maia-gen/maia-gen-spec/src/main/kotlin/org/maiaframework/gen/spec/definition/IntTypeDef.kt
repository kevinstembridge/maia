package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn


class IntTypeDef(
    fqcn: Fqcn,
    provided: Boolean
) : SimpleTypeDef(
    fqcn,
    Fqcns.INT_TYPE,
    FieldTypes.int,
    provided
)
