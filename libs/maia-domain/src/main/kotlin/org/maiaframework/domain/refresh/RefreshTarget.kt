package org.maiaframework.domain.refresh

import org.maiaframework.domain.DomainId


/**
 * Something derived from the database (an Elasticsearch index, a Hazelcast map) whose entries can be refreshed from it.
 */
interface RefreshTarget {


    /**
     * Must be idempotent and must rebuild each id from current database state, deleting the
     * entry when the source row no longer exists.
     */
    fun refreshByIds(ids: Collection<DomainId>)


    /**
     * Not invoked by [RefreshCollector]; for direct use by delete paths.
     */
    fun deleteById(id: DomainId)


}
