package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.AuthorityDef
import org.maiaframework.gen.spec.definition.EntityHistoryBlotterConfig

@MaiaDslMarker
class HistoryBlotterConfigBuilder {


    private var authorityDef: AuthorityDef? = null


    private var pathPrefix: String? = null


    fun authority(authorityDef: AuthorityDef) {

        this.authorityDef = authorityDef

    }


    fun pathPrefix(pathPrefix: String) {

        require(pathPrefix.startsWith("/") && !pathPrefix.endsWith("/")) {
            "pathPrefix must start with '/' and must not end with '/': '$pathPrefix'"
        }

        this.pathPrefix = pathPrefix

    }


    fun build(): EntityHistoryBlotterConfig {

        return EntityHistoryBlotterConfig(this.authorityDef, this.pathPrefix)

    }


}
