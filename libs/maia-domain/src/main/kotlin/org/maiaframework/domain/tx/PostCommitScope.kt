package org.maiaframework.domain.tx

/**
 * Marks the current thread as delivering work after its transaction has committed.
 *
 * Spring still reports transaction synchronization as active inside `afterCommit`, but a synchronization
 * registered at that point never fires. Code that would otherwise defer work to `afterCommit` checks [isActive]
 * and runs the work immediately instead.
 */
object PostCommitScope {


    private val active = ThreadLocal<Boolean>()


    val isActive: Boolean
        get() = active.get() == true


    fun <T> run(block: () -> T): T {

        val previous = active.get()
        active.set(true)

        try {
            return block()
        } finally {
            if (previous == null) active.remove() else active.set(previous)
        }

    }


}
