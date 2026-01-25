package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn


class StringValueClassDef(
    fqcn: Fqcn,
    provided: Boolean
) : ValueClassDef(
    fqcn,
    FieldTypes.string,
    provided
)
