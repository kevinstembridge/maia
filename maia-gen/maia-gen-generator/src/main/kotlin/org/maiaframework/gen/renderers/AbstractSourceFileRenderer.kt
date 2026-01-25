package org.maiaframework.gen.renderers


import java.io.File
import java.io.IOException
import java.io.PrintWriter

abstract class AbstractSourceFileRenderer : AbstractSourceRenderer() {


    fun renderToDir(outputDir: File) {

        val renderedSource = renderSource()
        val sourceFile = createSourceFile(outputDir)
        writeSourceToFile(renderedSource, sourceFile)

    }


    private fun createSourceFile(outputDir: File): File {

        val filePath = renderedFilePath()
        val sourceFile = File(outputDir, filePath)

        if (sourceFile.exists() == false) {

            val parentDir = sourceFile.parentFile

            if (parentDir.exists() == false) {

                val mkdirsSucceeded = parentDir.mkdirs()

                if (mkdirsSucceeded == false) {
                    throw RuntimeException("Unable to create parent directory for an unknown reason. [$parentDir]")
                }

            }

        }

        return sourceFile

    }


    protected abstract fun renderedFilePath(): String


    private fun writeSourceToFile(source: String, sourceFile: File) {

        try {
            PrintWriter(sourceFile).use { writer ->

                writer.write(source)

            }
        } catch (e: IOException) {
            throw RuntimeException("Error writing source to file", e)
        }

    }


    protected fun renderStrings(args: List<String>, indent: Int = 8, prefix: String = "") {

        val indentString = "".padStart(indent, ' ')

        val separator: String

        if (args.size > 1) {
            append("$indentString$prefix")
            separator = ",\n$indentString$prefix"
        } else {
            append("$indentString$prefix")
            separator = ", "
        }

        val textToRender = args.joinToString(separator)

        append(textToRender)

    }


}
