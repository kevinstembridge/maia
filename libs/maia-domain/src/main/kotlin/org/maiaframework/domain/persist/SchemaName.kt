package org.maiaframework.domain.persist

import org.maiaframework.lang.text.CaseHandling
import org.maiaframework.types.StringType

class SchemaName(value: String): StringType<SchemaName>(value, CaseHandling.LOWERCASE)
