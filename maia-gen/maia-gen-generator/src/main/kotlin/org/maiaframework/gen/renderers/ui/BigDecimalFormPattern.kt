package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.lang.BigDecimalFieldType

/**
 * Builds the regular expression a form uses to validate text entered for a BigDecimal field: an optional minus sign, at most
 * `precision - scale` integer digits, and at most `scale` decimal digits.  The syntax is common to Java and JavaScript.
 */
object BigDecimalFormPattern {


    fun regexFor(fieldType: BigDecimalFieldType): String {

        val integerDigits = fieldType.integerDigits
        val scale = fieldType.scale

        val integerPart = if (integerDigits > 0) "\\d{1,$integerDigits}" else "0"

        val decimalPart = if (scale > 0) "(\\.\\d{1,$scale})?" else ""

        return "^-?$integerPart$decimalPart$"

    }


}
