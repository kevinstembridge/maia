package org.maiaframework.common.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(private val prefix: String) : ThreadFactory {

    private val sequence = AtomicInteger(1)


    override fun newThread(runnable: Runnable): Thread {

        val thread = Thread(runnable)
        val seq = sequence.getAndIncrement()
        thread.name = prefix + if (seq > 1) "-$seq" else ""

        if (thread.isDaemon == false) {
            thread.isDaemon = true
        }

        return thread

    }


}
