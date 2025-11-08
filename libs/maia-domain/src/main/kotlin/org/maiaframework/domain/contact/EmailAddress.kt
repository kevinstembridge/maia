package org.maiaframework.domain.contact

import org.maiaframework.types.StringType
import java.util.*

class EmailAddress(value: String) : StringType<EmailAddress>(value.lowercase(Locale.getDefault()).trim())
