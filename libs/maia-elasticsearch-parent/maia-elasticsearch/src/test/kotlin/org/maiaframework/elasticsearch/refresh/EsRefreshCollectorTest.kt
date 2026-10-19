package org.maiaframework.elasticsearch.refresh

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.tx.PostCommitScope
import org.maiaframework.props.Props
import org.maiaframework.props.repo.InMemoryPropsRepo
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


class EsRefreshCollectorTest {


    private val calls = mutableListOf<Pair<String, List<DomainId>>>()


    private val target = object : EsRefreshTarget {

        override fun refreshByIds(ids: Collection<DomainId>) {
            calls += "refresh" to ids.toList()
        }

        override fun deleteById(id: DomainId) {
            calls += "delete" to listOf(id)
        }

    }


    private val collector = EsRefreshCollector(
        Props(
            InMemoryPropsRepo(),
            StandardEnvironment().apply {
                propertySources.addFirst(MapPropertySource("test", mapOf("maia.elasticsearch.refresh.chunkSize" to "2")))
            }
        )
    )


    private fun commit() {
        val synchronizations = TransactionSynchronizationManager.getSynchronizations().toList()
        synchronizations.forEach { it.afterCommit() }
        synchronizations.forEach { it.afterCompletion(TransactionSynchronization.STATUS_COMMITTED) }
    }


    @AfterEach
    fun cleanup() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
        TransactionSynchronizationManager.getResourceMap().keys.toList().forEach { TransactionSynchronizationManager.unbindResourceIfPossible(it) }
    }


    @Test
    fun `enlisting inside a post-commit scope joins work already pending so ids are deduplicated`() {

        TransactionSynchronizationManager.initSynchronization()
        val id = DomainId.newId()

        collector.enlistRefresh(target, listOf(id))

        PostCommitScope.run {
            collector.enlistRefresh(target, listOf(id))
        }

        assertThat(calls).isEmpty()

        commit()

        assertThat(calls).containsExactly("refresh" to listOf(id))

    }


    @Test
    fun `enlisting inside a post-commit scope flushes immediately even though synchronization is active`() {

        TransactionSynchronizationManager.initSynchronization()
        val id = DomainId.newId()

        PostCommitScope.run {
            collector.enlistRefresh(target, listOf(id))
            collector.enlistRefreshResolved(target) { listOf(id) }
        }

        assertThat(calls).containsExactly("refresh" to listOf(id), "refresh" to listOf(id))
        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty()

    }


    @Test
    fun `dedupes and chunks after commit`() {
        TransactionSynchronizationManager.initSynchronization()
        val a = DomainId.newId()
        val b = DomainId.newId()
        val c = DomainId.newId()
        collector.enlistRefresh(target, listOf(a, b, a, c))
        assertThat(calls).isEmpty()
        commit()
        assertThat(calls.map { it.second.size }).containsExactly(2, 1)
        assertThat(calls.flatMap { it.second }).containsExactlyInAnyOrder(a, b, c)
    }


    @Test
    fun `rollback flushes nothing`() {
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefresh(target, listOf(DomainId.newId()))
        TransactionSynchronizationManager.getSynchronizations().forEach { it.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK) }
        assertThat(calls).isEmpty()
    }


    @Test
    fun `no transaction flushes immediately`() {
        collector.enlistRefresh(target, listOf(DomainId.newId()))
        assertThat(calls).hasSize(1)
    }


    @Test
    fun `failing target does not prevent other targets from flushing`() {
        val failing = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) = throw IllegalStateException("boom")
            override fun deleteById(id: DomainId) = Unit
        }
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefresh(failing, listOf(DomainId.newId()))
        val ok = DomainId.newId()
        collector.enlistRefresh(target, listOf(ok))
        commit()
        assertThat(calls.flatMap { it.second }).containsExactly(ok)
    }


    @Test
    fun `clearing synchronization between transactions does not drop ids from the second`() {
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefresh(target, listOf(DomainId.newId()))
        TransactionSynchronizationManager.clearSynchronization()
        calls.clear()

        TransactionSynchronizationManager.initSynchronization()
        val second = DomainId.newId()
        collector.enlistRefresh(target, listOf(second))
        commit()

        assertThat(calls.flatMap { it.second }).containsExactly(second)
    }


    @Test
    fun `ids across separate enlist calls are deduped`() {
        TransactionSynchronizationManager.initSynchronization()
        val a = DomainId.newId()
        val b = DomainId.newId()
        collector.enlistRefresh(target, listOf(a))
        collector.enlistRefresh(target, listOf(a, b))
        commit()
        assertThat(calls.flatMap { it.second }).containsExactlyInAnyOrder(a, b)
    }


    @Test
    fun `enlisting during afterCommit flushes the new ids once`() {
        val other = mutableListOf<DomainId>()
        val otherTarget = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                other += ids
            }
            override fun deleteById(id: DomainId) = Unit
        }
        val x = DomainId.newId()
        val reentrant = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                collector.enlistRefresh(otherTarget, listOf(x))
            }
            override fun deleteById(id: DomainId) = Unit
        }
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefresh(reentrant, listOf(DomainId.newId()))
        commit()
        assertThat(other).containsExactly(x)
    }


    @Test
    fun `failed chunk does not skip remaining chunks`() {
        var first = true
        val flaky = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                if (first) {
                    first = false
                    throw IllegalStateException("boom")
                }
                calls += "refresh" to ids.toList()
            }
            override fun deleteById(id: DomainId) = Unit
        }
        TransactionSynchronizationManager.initSynchronization()
        val ids = List(4) { DomainId.newId() }
        collector.enlistRefresh(flaky, ids)
        commit()
        assertThat(calls.flatMap { it.second }).containsExactlyElementsOf(ids.drop(2))
    }


    @Test
    fun `resolver runs after commit and not at enlist`() {
        TransactionSynchronizationManager.initSynchronization()
        val a = DomainId.newId()
        var resolverCalls = 0
        collector.enlistRefreshResolved(target) {
            resolverCalls++
            listOf(a)
        }
        assertThat(resolverCalls).isEqualTo(0)
        assertThat(calls).isEmpty()
        commit()
        assertThat(resolverCalls).isEqualTo(1)
        assertThat(calls.flatMap { it.second }).containsExactly(a)
    }


    @Test
    fun `resolver ids are deduped with directly enlisted ids and chunked as one set`() {
        TransactionSynchronizationManager.initSynchronization()
        val a = DomainId.newId()
        val b = DomainId.newId()
        val c = DomainId.newId()
        collector.enlistRefresh(target, listOf(a, b))
        collector.enlistRefreshResolved(target) { listOf(b, c) }
        commit()
        assertThat(calls.map { it.second.size }).containsExactly(2, 1)
        assertThat(calls.flatMap { it.second }).containsExactlyInAnyOrder(a, b, c)
    }


    @Test
    fun `rollback never runs the resolver`() {
        TransactionSynchronizationManager.initSynchronization()
        var resolverCalls = 0
        collector.enlistRefreshResolved(target) {
            resolverCalls++
            listOf(DomainId.newId())
        }
        TransactionSynchronizationManager.getSynchronizations().forEach { it.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK) }
        assertThat(resolverCalls).isEqualTo(0)
        assertThat(calls).isEmpty()
    }


    @Test
    fun `resolver without transaction runs and flushes immediately`() {
        val a = DomainId.newId()
        collector.enlistRefreshResolved(target) { listOf(a) }
        assertThat(calls.flatMap { it.second }).containsExactly(a)
    }


    @Test
    fun `throwing resolver does not stop other targets nor lose its target's direct ids`() {
        val other = mutableListOf<DomainId>()
        val otherTarget = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                other += ids
            }
            override fun deleteById(id: DomainId) = Unit
        }
        TransactionSynchronizationManager.initSynchronization()
        val direct = DomainId.newId()
        val resolved = DomainId.newId()
        collector.enlistRefresh(target, listOf(direct))
        collector.enlistRefreshResolved(target) { throw IllegalStateException("boom") }
        collector.enlistRefreshResolved(otherTarget) { listOf(resolved) }
        commit()
        assertThat(calls.flatMap { it.second }).containsExactly(direct)
        assertThat(other).containsExactly(resolved)
    }


    @Test
    fun `resolved enlist during afterCommit resolves and flushes immediately`() {
        val other = mutableListOf<DomainId>()
        val otherTarget = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                other += ids
            }
            override fun deleteById(id: DomainId) = Unit
        }
        val x = DomainId.newId()
        val reentrant = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                collector.enlistRefreshResolved(otherTarget) { listOf(x) }
            }
            override fun deleteById(id: DomainId) = Unit
        }
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefresh(reentrant, listOf(DomainId.newId()))
        commit()
        assertThat(other).containsExactly(x)
    }


    @Test
    fun `resolver that enlists both ways during afterCommit flushes both immediately`() {
        val other = mutableListOf<DomainId>()
        val otherTarget = object : EsRefreshTarget {
            override fun refreshByIds(ids: Collection<DomainId>) {
                other += ids
            }
            override fun deleteById(id: DomainId) = Unit
        }
        val x = DomainId.newId()
        val y = DomainId.newId()
        val a = DomainId.newId()
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefreshResolved(target) {
            collector.enlistRefresh(otherTarget, listOf(x))
            collector.enlistRefreshResolved(otherTarget) { listOf(y) }
            listOf(a)
        }
        commit()
        assertThat(other).containsExactly(x, y)
        assertThat(calls.flatMap { it.second }).containsExactly(a)
    }


    @Test
    fun `enlistRefreshNow resolves immediately and flushes after commit`() {
        val a = DomainId.newId()
        TransactionSynchronizationManager.initSynchronization()
        var resolved = false
        collector.enlistRefreshNow(target) { resolved = true; listOf(a) }
        assertThat(resolved).isTrue()
        assertThat(calls).isEmpty()
        commit()
        assertThat(calls.flatMap { it.second }).containsExactly(a)
    }


    @Test
    fun `enlistRefreshNow swallows a throwing resolver and enlists nothing`() {
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefreshNow(target) { throw IllegalStateException("boom") }
        commit()
        assertThat(calls).isEmpty()
    }


}
