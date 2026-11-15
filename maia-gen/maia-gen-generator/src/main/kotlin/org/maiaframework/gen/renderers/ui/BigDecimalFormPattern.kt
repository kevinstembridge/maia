package org.maiaframework.gen.renderers.ui

/**
 * Builds the regular expression a form uses to validate text entered for a BigDecimal field: an optional minus sign, at most
 * `precision - scale` integer digits, and at most `scale` decimal digits.  The syntax is common to Java and JavaScript.
 */
object BigDecimalFormPattern {


    fun regexFor(precision: Int, scale: Int): String {

        val integerDigits = precision - scale

        val integerPart = if (integerDigits > 0) "\\d{1,$integerDigits}" else "0"

        val decimalPart = if (scale > 0) "(\\.\\d{1,$scale})?" else ""

        return "^-?$integerPart$decimalPart$"

    }


}
