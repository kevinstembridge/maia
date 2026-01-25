package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.CacheName
import org.maiaframework.gen.spec.definition.CacheableDef

class CacheableDefBuilder(var cacheName: CacheName) {


    fun build(): CacheableDef {

        return CacheableDef(
            cacheName
        )

    }


}
