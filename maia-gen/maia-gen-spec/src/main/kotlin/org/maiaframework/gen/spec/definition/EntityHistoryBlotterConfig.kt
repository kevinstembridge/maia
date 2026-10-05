package org.maiaframework.gen.spec.definition


/**
 * Optional customisation of the generated history blotter endpoints and Angular service.
 *
 * @param authorityDef if set, the generated search and count endpoints require this authority
 * @param pathPrefix replaces the default `/api` prefix of the generated endpoint and Angular service URLs
 */
class EntityHistoryBlotterConfig(
    val authorityDef: AuthorityDef?,
    val pathPrefix: String?
) {


    companion object {

        val DEFAULT = EntityHistoryBlotterConfig(authorityDef = null, pathPrefix = null)

    }


}
