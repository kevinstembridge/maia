package org.maiaframework.elasticsearch.index.model

import org.maiaframework.lang.text.StringFunctions

data class EsIndexVersion(val value: Int) {


    override fun toString(): String {
        return "_v${StringFunctions.padWithLeadingZeroes(this.value, 4)}"
    }


}
