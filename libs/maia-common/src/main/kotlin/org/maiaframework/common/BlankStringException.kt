package org.maiaframework.common


/**
 * Indicates that a string was either null, empty or consisted only of whitespace.
 */
class BlankStringException private constructor(val argumentName: String) : RuntimeException("The [$argumentName] argument cannot be null or blank.") {

    companion object {


        @JvmStatic
        fun throwIfBlank(argumentValue: String?, argumentName: String): String {

            if (argumentValue == null || "" == argumentValue.trim { it <= ' ' }) {
                throw BlankStringException(argumentName)
            }

            return argumentValue

        }
    }


}
