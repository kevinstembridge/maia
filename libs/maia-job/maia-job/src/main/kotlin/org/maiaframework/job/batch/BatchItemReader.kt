package org.maiaframework.job.batch

interface BatchItemReader<T> {

    /**
     * Returns null if there are no more items to be read.
     */
    fun readItem(): T?

}
