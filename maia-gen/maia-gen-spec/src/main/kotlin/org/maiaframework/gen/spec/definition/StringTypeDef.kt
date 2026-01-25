package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn


class StringTypeDef(
    fqcn: Fqcn,
    provided: Boolean,
    val caseMode: CaseMode
) : SimpleTypeDef(
    fqcn,
    Fqcns.STRING_TYPE,
    FieldTypes.string,
    provided
) {


    enum class CaseMode {

        ALWAYS_UPPER,
        ALWAYS_LOWER,
        AS_PROVIDED

    }


}
