package org.maiaframework.hazelcast

import com.hazelcast.nio.serialization.compact.CompactSerializer

data class HazelcastSerializerDef<T>(
    val serializer: CompactSerializer<T>
)
