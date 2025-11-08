package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn


class LongTypeDef(
    fqcn: Fqcn,
    provided: Boolean
) : SimpleTypeDef(
    fqcn,
    Fqcns.LONG_TYPE,
    FieldTypes.long,
    provided
)
