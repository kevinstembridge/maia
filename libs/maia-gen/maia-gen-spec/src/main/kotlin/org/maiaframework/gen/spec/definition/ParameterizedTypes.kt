package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.NonPrimitiveType
import org.maiaframework.gen.spec.definition.lang.ParameterizedType

object ParameterizedTypes {

    val SPRING_INITIALIZING_BEAN = ParameterizedType(Fqcns.SPRING_INITIALIZING_BEAN)

    val SPRING_APPLICATION_CONTEXT_AWARE = ParameterizedType(Fqcns.SPRING_APPLICATION_CONTEXT_AWARE)

    fun hazelcastCompactSerializer(nonPrimitiveType: NonPrimitiveType) = ParameterizedType(Fqcns.HAZELCAST_COMPACT_SERIALIZER, nonPrimitiveType)

}
