package org.maiaframework.elasticsearch.refresh

import org.maiaframework.domain.DomainId


/**
 * Something whose Elasticsearch documents can be refreshed from the database.
 */
interface EsRefreshTarget {


    /**
     * Must be idempotent and must rebuild each id from current database state, deleting the
     * document when the source row no longer exists.
     */
    fun refreshByIds(ids: Collection<DomainId>)


    /**
     * Not invoked by [EsRefreshCollector]; for direct use by delete paths.
     */
    fun deleteById(id: DomainId)


}
