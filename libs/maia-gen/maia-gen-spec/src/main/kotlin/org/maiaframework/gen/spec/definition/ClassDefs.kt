package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder

object ClassDefs {


    val hazelcastEntityConfig = ClassDefBuilder.aClassDef(
        Fqcns.MAHANA_HAZELCAST_ENTITY_CONFIG
    ).withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


}
