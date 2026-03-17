package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.CacheName
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.CacheableDef

@MaiaDslMarker
class EntityCacheableDefBuilder(private val entityBaseName: EntityBaseName) {


    var cacheName: String? = null


    fun build(): CacheableDef {

        val cacheName = cacheName?.let { CacheName(it) } ?: CacheName(entityBaseName.withSuffix("Entity").toSnakeCase())

        return CacheableDef(
            cacheName
        )

    }


}
