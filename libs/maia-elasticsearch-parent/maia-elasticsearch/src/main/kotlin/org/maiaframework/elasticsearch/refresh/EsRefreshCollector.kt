package org.maiaframework.elasticsearch.refresh

import org.maiaframework.domain.refresh.RefreshCollector
import org.maiaframework.props.Props


/**
 * The [RefreshCollector] of Elasticsearch documents: failures are logged and the scheduled `refreshIndex` job
 * reconciles. The chunk size is the `maia.elasticsearch.refresh.chunkSize` property (default 1000).
 */
class EsRefreshCollector(
    props: Props
) : RefreshCollector(
    { props.getIntOrNull("maia.elasticsearch.refresh.chunkSize") ?: 1000 },
    "ES",
    "refreshIndex job will reconcile"
)
