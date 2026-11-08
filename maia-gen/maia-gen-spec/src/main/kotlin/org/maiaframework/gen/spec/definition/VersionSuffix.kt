package org.maiaframework.gen.spec.definition

import org.maiaframework.lang.text.StringFunctions.padWithLeadingZeroes

/**
 * Formats a version as the trailing suffix of a generated name: `_v0004` for class names, `-v0004` for kebab-case
 * names (TypeScript files, selectors).
 */
object VersionSuffix {

    fun of(version: Int): String = "_v${padded(version)}"

    fun kebab(version: Int): String = "-v${padded(version)}"

    private fun padded(version: Int) = padWithLeadingZeroes(version, 4)

}
