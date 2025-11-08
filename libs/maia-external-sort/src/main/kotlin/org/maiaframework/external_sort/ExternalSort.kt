package org.maiaframework.external_sort

import org.maiaframework.metrics.JobMetrics
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.Deflater
import java.util.zip.GZIPOutputStream

object ExternalSort {

    private val logger = LoggerFactory.getLogger(ExternalSort::class.java)
    private val numberFormat = NumberFormat.getIntegerInstance()
    private val percentFormat = NumberFormat.getPercentInstance()

    private val defaultComparator: Comparator<String> = Comparator { r1, r2 -> r1.compareTo(r2) }

    private const val DEFAULT_MAX_TEMP_FILES = 1024


    fun sort(
        inputFile: File,
        outputFile: File,
        comparator: Comparator<String> = defaultComparator,
        discardDuplicateLines: Boolean = false,
        useGzip: Boolean = false
    ) {

        val jobMetrics = JobMetrics("externalSort")

        jobMetrics.timeInstanceOfJob {

            val sortedFiles = sortInBatch(
                    inputFile = inputFile,
                    comparator = comparator,
                    useGzip = useGzip,
                    jobMetrics = jobMetrics
            )

            mergeSortedFiles(
                    sortedFiles,
                    outputFile,
                    comparator = comparator,
                    discardDuplicateLines = discardDuplicateLines,
                    jobMetrics = jobMetrics
            )

        }

        logger.info("Metrics for external sort:\n${jobMetrics.getMetricsReportAsJson()}")

    }


    /**
     * Merges a collection of temporary sorted files.
     */
    private fun mergeSortedFiles(
            sortedFiles: List<SortedFile>,
            outputFile: File,
            comparator: Comparator<String> = defaultComparator,
            charset: Charset = Charset.defaultCharset(),
            discardDuplicateLines: Boolean = false,
            append: Boolean = false,
            jobMetrics: JobMetrics
    ) {

        createDirIfNotExists(outputFile.parentFile)
        val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(outputFile, append), charset))
        mergeSortedFiles(sortedFiles, bufferedWriter, comparator, discardDuplicateLines, jobMetrics)

        sortedFiles.forEach { it.delete() }

    }


    /**
     * This merges several sorted files to an output writer.
     */
    private fun mergeSortedFiles(
            sortedFiles: List<SortedFile>,
            outputWriter: BufferedWriter,
            comparator: Comparator<String>,
            discardDuplicateLines: Boolean,
            parentJobMetrics: JobMetrics
    ) {

        val startTime = Instant.now()

        val comparatorFunc = Comparator<SortedFileReader> { i, j ->
            parentJobMetrics.timeChildJobWithResult("compareInPriorityQueue") {
                comparator.compare(i.peek(), j.peek())
            }
        }

        val readerQueue = PriorityQueue(11, comparatorFunc)

        val totalLineCount = sortedFiles.sumOf { it.lineCount }
        sortedFiles.asSequence().map { it.getBuffer() }.filter { it.notEmpty() }.forEach { readerQueue.add(it) }

        var rowCounter = 0
        var previousLine: String? = null

        try {
            while (readerQueue.size > 0) {
                val reader = readerQueue.poll()
                val line = reader.pop()

                parentJobMetrics.timeChildJob("skipDuplicates") {

                    if (discardDuplicateLines == false || previousLine == null || comparator.compare(line, previousLine) != 0) {
                        outputWriter.write(line)
                        outputWriter.newLine()
                        previousLine = line
                    }

                }

                ++rowCounter

                if (rowCounter % 10_000 == 0) {
                    val percentage: Double = rowCounter / (totalLineCount * 1.0)
                    val lapTime = Instant.now()
                    val duration = Duration.between(startTime, lapTime)
                    val elapsedMillis = lapTime.toEpochMilli() - startTime.toEpochMilli()
                    val millisPerLine = elapsedMillis / (totalLineCount * 1.0)
                    logger.info("Sorted ${numberFormat.format(rowCounter)} of ${numberFormat.format(totalLineCount)} lines (${percentFormat.format(percentage)}) in $duration ($millisPerLine ms per line).")
                    logger.debug(parentJobMetrics.getMetricsReportAsJson())
                }

                if (reader.empty()) {
                    reader.close()
                } else {
                    parentJobMetrics.timeChildJob("pushToPriorityQueue") {
                        readerQueue.add(reader)
                    }
                }
            }

        } finally {

            outputWriter.close()

            for (reader in readerQueue) {
                reader?.close()
            }

        }

    }


    private fun sortAndSaveToTempFile(
            lines: List<String>,
            comparator: Comparator<String>,
            charset: Charset,
            tmpDirectory: File?,
            discardDuplicateLines: Boolean = false,
            useGzip: Boolean = false,
            parentJobMetrics: JobMetrics
    ): SortedFile {

        return parentJobMetrics.timeChildJobWithResult("sortAndSaveToTempFile") { jobMetrics ->

            val sortedLines = jobMetrics.timeChildJobWithResult("sortedLines") {
                lines.sortedWith(comparator)
            }

            val tempFile = initTempFile(tmpDirectory, useGzip)
            val bufferedWriter = initBufferedWriter(tempFile, useGzip, charset)
            val lineCount = AtomicInteger()

            var previousLine: String? = null

            bufferedWriter.use { writer ->
                for (currentLine in sortedLines) {

                    jobMetrics.timeChildJob("processLine") {

                        if (discardDuplicateLines == false || previousLine == null || comparator.compare(currentLine, previousLine) != 0) {
                            writer.write(currentLine)
                            writer.newLine()
                            lineCount.incrementAndGet()
                            previousLine = currentLine
                        }

                    }
                }
            }

            logger.info("Saving sorted temp file: ${tempFile.name}")
            logger.debug(parentJobMetrics.getMetricsReportAsJson())

            SortedFile(tempFile, charset, useGzip, lineCount.get())

        }

    }


    private fun initTempFile(tmpDirectory: File?, useGzip: Boolean): File {

        val newTempFile = File.createTempFile("externalSort-split-", ".txt${if (useGzip) ".gz" else ""}", tmpDirectory)
        newTempFile.deleteOnExit()
        return newTempFile

    }


    private fun initBufferedWriter(file: File, useGzip: Boolean, charset: Charset): BufferedWriter {

        val out: OutputStream = initOutputStream(file, useGzip)
        return BufferedWriter(OutputStreamWriter(out, charset))

    }


    private fun initOutputStream(newTempFile: File, useGzip: Boolean): OutputStream {

        val outputStream = FileOutputStream(newTempFile)

        if (useGzip) {
            return object : GZIPOutputStream(outputStream, 2048) {
                init {
                    this.def.setLevel(Deflater.BEST_COMPRESSION)
                }
            }
        } else {
            return outputStream
        }

    }


    /**
     * This will simply load the file by blocks of lines, the sort them in-memory, and write the result
     * to temporary files that have to be merged later. You can specify a bound on the number of temporary
     * files that will be created.
     */
    fun sortInBatch(
            inputFile: File,
            comparator: Comparator<String> = defaultComparator,
            maxNumberOfTempFiles: Int = DEFAULT_MAX_TEMP_FILES,
            charset: Charset = Charset.defaultCharset(),
            tmpDirectory: File? = null,
            discardDuplicateLines: Boolean = false,
            numberOfHeaderLines: Int = 0,
            useGzip: Boolean = false,
            jobMetrics: JobMetrics
    ): List<SortedFile> {

        val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(inputFile), charset))

        return sortInBatch(
                bufferedReader,
                inputFile.length(),
                comparator,
                maxNumberOfTempFiles,
                estimateAvailableMemory(),
                charset,
                tmpDirectory,
                discardDuplicateLines,
                numberOfHeaderLines,
                useGzip,
                jobMetrics
        )

    }


    fun sortInBatch(
        bufferedReader: BufferedReader,
        dataLength: Long,
        comparator: Comparator<String> = defaultComparator,
        maxNumberOfTempFiles: Int = DEFAULT_MAX_TEMP_FILES,
        maxMemory: Long = estimateAvailableMemory(),
        charset: Charset = Charset.defaultCharset(),
        tmpDirectory: File? = null,
        discardDuplicateLines: Boolean = false,
        numberOfHeaderLines: Int = 0,
        useGzip: Boolean = false,
        parentJobMetrics: JobMetrics
    ): List<SortedFile> {

        val sortedFiles = mutableListOf<SortedFile>()
        val currentLines = mutableListOf<String>()
        val blockSizeInBytes = estimateBestSizeOfBlocks(dataLength, maxNumberOfTempFiles, maxMemory)

        parentJobMetrics.timeChildJobWithResult("sortInBatch") { jobMetrics ->

            bufferedReader.use { br ->
                var currentLine: String? = ""
                try {
                    var counter = 0
                    while (currentLine != null) {
                        var currentBlockSizeInBytes: Long = 0
                        currentLine = br.readLine()
                        while (currentBlockSizeInBytes < blockSizeInBytes && currentLine != null) {
                            // as long as you have enough memory
                            if (counter < numberOfHeaderLines) {
                                counter++
                                continue
                            }
                            currentLines.add(currentLine)
                            currentBlockSizeInBytes += StringSizeEstimator.estimatedSizeOf(currentLine)

                            currentLine = br.readLine()
                        }

                        sortedFiles.add(sortAndSaveToTempFile(currentLines, comparator, charset, tmpDirectory, discardDuplicateLines, useGzip, jobMetrics))
                        currentLines.clear()

                    }

                } catch (e: EOFException) {
                    if (currentLines.size > 0) {
                        sortedFiles.add(sortAndSaveToTempFile(currentLines, comparator, charset, tmpDirectory, discardDuplicateLines, useGzip, jobMetrics))
                        currentLines.clear()
                    }
                }

            }

        }

        return sortedFiles

    }


    /**
     * We divide the file into small blocks. If the blocks are too small, we'll create too many
     * temp files. If the blocks are too big, we'll be using too much memory.
     */
    private fun estimateBestSizeOfBlocks(
            sizeOfFile: Long,
            maxNumberOfTempFiles: Int,
            maxMemory: Long
    ): Long {

        // We don't want to opn up much more than maxNumberOfTempFiles temporary files, better to run out
        // of memory first.
        val blockSize = sizeOfFile / maxNumberOfTempFiles + if (sizeOfFile % maxNumberOfTempFiles == 0L) 0 else 1

        // On the other hand, we don't want to create many temporary files for nothing.
        // If blockSize is smaller than half the free memory, grow it.
        if (blockSize < maxMemory / 2) {
            return maxMemory / 2
        } else {
            return blockSize
        }

    }


    /**
     * Calls the garbage collector and then returns the free memory.
     * This avoids problems with applications where the GC hasn't reclaimed
     * memory and reports no available memory.
     */
    private fun estimateAvailableMemory(): Long {

        System.gc()
        return Runtime.getRuntime().freeMemory()

    }


    private fun createDirIfNotExists(dir: File): Boolean {

        return dir.exists() || dir.mkdirs()

    }


}
