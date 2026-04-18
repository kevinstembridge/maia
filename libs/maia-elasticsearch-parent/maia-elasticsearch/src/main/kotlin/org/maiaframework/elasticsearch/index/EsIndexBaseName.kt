package org.maiaframework.elasticsearch.index

import org.maiaframework.types.StringType
import java.util.*

class EsIndexBaseName(value: String): StringType<EsIndexBaseName>(value.trim().lowercase(Locale.getDefault())) {


    fun withSuffix(suffix: String): EsIndexBaseName {

        return EsIndexBaseName("${value}$suffix")

    }


}
