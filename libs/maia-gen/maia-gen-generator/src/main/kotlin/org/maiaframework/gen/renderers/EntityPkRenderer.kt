package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef


class EntityPkRenderer(
    entityDef: EntityDef
) : AbstractKotlinRenderer(
    entityDef.entityPkClassDef
)
