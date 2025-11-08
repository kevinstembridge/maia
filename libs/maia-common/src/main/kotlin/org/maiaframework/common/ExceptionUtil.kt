package org.maiaframework.common

import java.io.PrintWriter
import java.io.StringWriter

object ExceptionUtil {

    fun stackTrace(e: Exception): String {

        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        return sw.toString()

    }

}
