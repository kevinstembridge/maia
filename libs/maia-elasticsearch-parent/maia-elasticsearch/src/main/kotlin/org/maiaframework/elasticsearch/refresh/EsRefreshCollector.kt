package org.maiaframework.elasticsearch.refresh

import org.maiaframework.domain.DomainId
import org.maiaframework.props.Props
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


/**
 * Collects ids to refresh in Elasticsearch and runs the refreshes after the surrounding transaction
 * commits, or immediately if no transaction synchronization is active.
 *
 * Ids are deduplicated per target and delivered in chunks. Exceptions from a target
 * are logged, not rethrown. Targets must not rely on being called inside the transaction.
 *
 * [enlistRefreshResolved] defers working out the ids until after commit, for ids that can only be
 * known from committed state (e.g. the roots an entity belongs to after an update). Resolvers run in
 * `afterCommit`, in enlistment order, immediately before the flush; their ids are merged (deduplicated)
 * with the ids enlisted directly for the same target and chunked as one set. A rolled-back transaction
 * never runs its resolvers. A resolver that throws is logged and contributes no ids; the other
 * resolvers and every directly enlisted id are still flushed.
 *
 * Without an active transaction synchronization, or when called from within `afterCommit` (after this
 * collector has flushed the current transaction), both enlist methods resolve and flush immediately.
 */
class EsRefreshCollector(
    private val props: Props
) {


    private val logger = LoggerFactory.getLogger(javaClass)


    private class Pending(
        val work: MutableMap<EsRefreshTarget, MutableSet<DomainId>> = linkedMapOf(),
        val resolvers: MutableMap<EsRefreshTarget, MutableList<() -> Collection<DomainId>>> = linkedMapOf()
    ) {

        lateinit var synchronization: TransactionSynchronization

        var flushed = false

    }


    fun enlistRefresh(
        target: EsRefreshTarget,
        ids: Collection<DomainId>
    ) {

        if (ids.isEmpty()) return

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            flush(mapOf(target to ids.toSet()))
            return
        }

        val pending = currentPending() ?: newPending()

        if (pending.flushed) {
            flush(mapOf(target to ids.toSet()))
            return
        }

        pending.work.getOrPut(target) { linkedSetOf() }.addAll(ids)

    }


    /**
     * Like [enlistRefresh], but the ids are produced by [resolver] after the transaction commits.
     */
    fun enlistRefreshResolved(
        target: EsRefreshTarget,
        resolver: () -> Collection<DomainId>
    ) {

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            flush(mapOf(target to resolve(target, resolver).toSet()))
            return
        }

        val pending = currentPending() ?: newPending()

        if (pending.flushed) {
            flush(mapOf(target to resolve(target, resolver).toSet()))
            return
        }

        pending.resolvers.getOrPut(target) { mutableListOf() }.add(resolver)

    }


    /**
     * Like [enlistRefresh], but the ids are produced by [resolver] now, inside the caller's transaction, for ids
     * that can only be known before the change commits (e.g. the roots joined to a row about to be deleted). A
     * resolver that throws is logged and enlists nothing, so the caller carries on and the refreshIndex job
     * reconciles. Note that a failed SQL statement in PostgreSQL still aborts the surrounding transaction.
     */
    fun enlistRefreshNow(
        target: EsRefreshTarget,
        resolver: () -> Collection<DomainId>
    ) {

        enlistRefresh(
            target,
            resolve(target, resolver)
        )

    }


    /**
     * The bound resource survives [TransactionSynchronizationManager.clearSynchronization], so only
     * trust it if its synchronization is still registered for the current transaction.
     */
    private fun currentPending(): Pending? {

        val pending = TransactionSynchronizationManager.getResource(this) as? Pending ?: return null
        return if (TransactionSynchronizationManager.getSynchronizations().contains(pending.synchronization)) pending else null

    }


    private fun newPending(): Pending {

        TransactionSynchronizationManager.unbindResourceIfPossible(this)

        val pending = Pending()

        pending.synchronization = object : TransactionSynchronization {

            override fun afterCommit() {
                pending.flushed = true
                flush(resolvedWork(pending))
            }

            override fun afterCompletion(status: Int) {
                TransactionSynchronizationManager.unbindResourceIfPossible(this@EsRefreshCollector)
            }

        }

        TransactionSynchronizationManager.bindResource(this, pending)
        TransactionSynchronizationManager.registerSynchronization(pending.synchronization)

        return pending

    }


    private fun resolvedWork(pending: Pending): Map<EsRefreshTarget, Set<DomainId>> {

        val work = linkedMapOf<EsRefreshTarget, MutableSet<DomainId>>()
        pending.work.forEach { (target, ids) -> work[target] = LinkedHashSet(ids) }

        pending.resolvers.forEach { (target, resolvers) ->
            resolvers.forEach { resolver ->
                work.getOrPut(target) { linkedSetOf() }.addAll(resolve(target, resolver))
            }
        }

        return work

    }


    private fun resolve(
        target: EsRefreshTarget,
        resolver: () -> Collection<DomainId>
    ): Collection<DomainId> {

        return try {
            resolver()
        } catch (e: Exception) {
            logger.error("ES refresh id resolution failed for ${target.javaClass.simpleName}; refreshIndex job will reconcile", e)
            emptyList()
        }

    }


    private fun flush(work: Map<EsRefreshTarget, Set<DomainId>>) {

        val chunkSize = this.props.getIntOrNull("maia.elasticsearch.refresh.chunkSize") ?: 1000

        work.forEach { (target, ids) ->
            ids.chunked(chunkSize).forEach { chunk ->
                try {
                    target.refreshByIds(chunk)
                } catch (e: Exception) {
                    logger.error("ES refresh failed for ${target.javaClass.simpleName} (chunk of ${chunk.size}); refreshIndex job will reconcile", e)
                }
            }
        }

    }


}
