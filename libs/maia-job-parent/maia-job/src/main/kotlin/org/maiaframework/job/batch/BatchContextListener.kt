package org.maiaframework.job.batch

interface BatchContextListener {

    fun beforeBatch(context: Map<String, Any>)

}
