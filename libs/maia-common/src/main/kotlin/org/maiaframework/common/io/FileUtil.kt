package org.maiaframework.common.io

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


object FileUtil {


    fun createDirIfNotExists(dir: File): Boolean {

        return dir.exists() || dir.mkdirs()

    }


    fun zipFiles(vararg sourceFiles: File, outputFile: File) {

        val fos = FileOutputStream(outputFile)
        val zipOut = ZipOutputStream(fos)

        for (fileToZip in sourceFiles) {

            val fis = FileInputStream(fileToZip)
            val zipEntry = ZipEntry(fileToZip.name)
            zipOut.putNextEntry(zipEntry)

            val bytes = ByteArray(1024)
            var length: Int = fis.read(bytes)

            while (length >= 0) {
                zipOut.write(bytes, 0, length)
                length = fis.read(bytes)
            }

            fis.close()

        }

        zipOut.close()
        fos.close()

    }


    fun zipDirectory(sourceDir: File, outputFile: File) {

        val fos = FileOutputStream(outputFile)
        val zipOut = ZipOutputStream(fos)

        zipFile(sourceDir, sourceDir.name, zipOut)
        zipOut.close()
        fos.close()

    }


    private fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {

        if (fileToZip.isHidden) {
            return
        }

        if (fileToZip.isDirectory) {

            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(ZipEntry(fileName))
                zipOut.closeEntry()
            } else {
                zipOut.putNextEntry(ZipEntry("$fileName/"))
                zipOut.closeEntry()
            }

            val children = fileToZip.listFiles()

            for (childFile in children!!) {
                zipFile(childFile, fileName + "/" + childFile.name, zipOut)
            }

            return

        }

        val fis = FileInputStream(fileToZip)
        val zipEntry = ZipEntry(fileName)
        zipOut.putNextEntry(zipEntry)
        val bytes = ByteArray(1024)

        var length = fis.read(bytes)

        while (length >= 0) {
            zipOut.write(bytes, 0, length)
            length = fis.read(bytes)
        }

        fis.close()

    }


}
