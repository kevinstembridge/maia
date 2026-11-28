package org.maiaframework.showcase.hazelcast_blotter

import com.hazelcast.collection.ISet
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap


/**
 * Test support for Hazelcast-backed Blotter state. The application context starts once per test class, so a "restart"
 * is simulated by publishing the context-refreshed event. A real restart of the only node of a cluster also loses the
 * cluster's memory, and the population rules rely on that (a same-shape start leaves a populated map alone), so a
 * simulated restart first clears every Blotter map and its control, staging and changed structures.
 */
object HazelcastBlotterTestState {


    fun clearAll(hazelcastInstance: HazelcastInstance) {

        hazelcastInstance.distributedObjects
            .filter { it.name.contains(BLOTTER_NAME_PART) }
            .forEach { obj ->
                when (obj) {
                    is IMap<*, *> -> obj.clear()
                    is ISet<*> -> obj.clear()
                }
            }

    }


    private const val BLOTTER_NAME_PART = "_blotter"


}
