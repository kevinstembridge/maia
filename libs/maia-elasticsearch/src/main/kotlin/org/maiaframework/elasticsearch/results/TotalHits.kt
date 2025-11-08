package org.maiaframework.elasticsearch.results

import co.elastic.clients.elasticsearch.core.search.HitsMetadata


data class TotalHits(val count: Int, val relation: TotalHitsRelation) {


    companion object {

        val EMPTY = TotalHits(0, TotalHitsRelation.eq)


        fun from(searchHits: HitsMetadata<*>): TotalHits {

            val totalHits = searchHits.total()
                    ?: return EMPTY

            val relation = when (totalHits.relation()) {
                co.elastic.clients.elasticsearch.core.search.TotalHitsRelation.Eq -> TotalHitsRelation.eq
                co.elastic.clients.elasticsearch.core.search.TotalHitsRelation.Gte -> TotalHitsRelation.gte
                else -> TotalHitsRelation.eq
            }

            return TotalHits(totalHits.value().toInt(), relation)

        }


    }


}
