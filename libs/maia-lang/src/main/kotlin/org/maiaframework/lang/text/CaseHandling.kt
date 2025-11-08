package org.maiaframework.lang.text

import java.util.*

enum class CaseHandling {

    UPPERCASE {
        override fun apply(input: String): String {
            return input.uppercase(Locale.getDefault())
        }
    },

    LOWERCASE {
        override fun apply(input: String): String {
            return input.lowercase(Locale.getDefault())
        }
    },

    ORIGINAL {
        override fun apply(input: String): String {
            return input
        }
    };

    abstract fun apply(input: String): String

}
