package org.maiaframework.domain.money

import org.maiaframework.types.StringType
import java.util.Locale

class CurrencyCode(value: String) : StringType<CurrencyCode>(value.uppercase(Locale.ENGLISH)) {

    companion object {

        val USD = CurrencyCode("USD")

    }

}
