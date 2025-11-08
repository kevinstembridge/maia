package org.maiaframework.gen.spec.definition

import org.maiaframework.lang.text.CaseHandling
import org.maiaframework.types.StringType

class AppKey(value: String) : StringType<AppKey>(value, caseHandling = CaseHandling.LOWERCASE)
