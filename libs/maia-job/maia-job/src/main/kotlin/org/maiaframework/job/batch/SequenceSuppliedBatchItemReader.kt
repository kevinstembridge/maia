package org.maiaframework.job.batch


class SequenceSuppliedBatchItemReader<T>(
    private val streamSupplier: () -> Sequence<T>,
): BatchItemReader<T>, BatchItemStream {


    private lateinit var itemStream: Sequence<T>


    private lateinit var itemIterator: Iterator<T>


    override fun openItemStream() {

        this.itemStream = this.streamSupplier.invoke()
        this.itemIterator = this.itemStream.iterator()

    }


    override fun readItem(): T? {

        return if (this.itemIterator.hasNext()) {
            this.itemIterator.next()
        } else {
            null
        }

    }


    override fun closeItemStream() {

        // do nothing

    }


}
