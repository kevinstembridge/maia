package org.maiaframework.external_sort

/**
 * A thin wrapper over a BufferedReader which holds the last line in memory.
 */
class SortedFileReader(sortedFile: SortedFile): AutoCloseable {

    private val reader = sortedFile.bufferedReader()
    private var cachedLine: String? = null


    init {
        cacheNextLine()
    }


    override fun close() {
        this.reader.close()
    }


    fun empty(): Boolean {
        return this.cachedLine == null
    }


    fun notEmpty(): Boolean {
        return empty() == false
    }


    fun peek(): String? {
        return this.cachedLine
    }


    fun pop(): String {

        val answer = peek()!!.toString()
        cacheNextLine()
        return answer
    }


    private fun cacheNextLine() {
        this.cachedLine = this.reader.readLine()
    }


}
