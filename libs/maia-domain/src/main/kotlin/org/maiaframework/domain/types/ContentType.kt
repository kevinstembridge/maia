package org.maiaframework.domain.types

import org.maiaframework.types.StringType

class ContentType(value: String) : StringType<ContentType>(value) {


    companion object {

        val APPLICATION_ZIP = ContentType("application/zip")

        val TEXT_PLAIN: ContentType = ContentType("text/plain")

    }


}
