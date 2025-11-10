package org.maiaframework.gen.spec

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassType

class EntityPkDef(
    val entityDef: EntityDef,
) {


    val fields = entityDef.allEntityFields.filter { it.isPrimaryKey.value }


    val classDef = aClassDef(this.entityDef.entityFqcn.withSuffix("Pk"))
        .withFieldDefsNotInherited(fields.map { it.classFieldDef })
        .ofType(ClassType.DATA_CLASS)
        .build()




}
