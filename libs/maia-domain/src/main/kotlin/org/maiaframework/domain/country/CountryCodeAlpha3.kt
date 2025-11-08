package org.maiaframework.domain.country

import org.maiaframework.types.StringType
import java.util.Locale

class CountryCodeAlpha3(value: String) : StringType<CountryCodeAlpha3>(value.uppercase(Locale.getDefault()).trim())
