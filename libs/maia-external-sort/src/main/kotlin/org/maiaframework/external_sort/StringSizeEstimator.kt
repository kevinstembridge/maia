package org.maiaframework.external_sort

object StringSizeEstimator {

    private val OBJECT_HEADER: Int
    private val ARRAY_HEADER: Int
    private const val INT_FIELDS = 12
    private val OBJECT_REF: Int
    private val OBJECT_OVERHEAD: Int
    private val IS_64_BIT_JVM: Boolean


    init {

        val arch = System.getProperty("sun.arch.data.model")
        IS_64_BIT_JVM = !(arch != null && arch.contains("32"))

        OBJECT_HEADER = if (IS_64_BIT_JVM) 16 else 8
        ARRAY_HEADER = if (IS_64_BIT_JVM) 24 else 12
        OBJECT_REF = if (IS_64_BIT_JVM) 8 else 4
        OBJECT_OVERHEAD = OBJECT_HEADER + INT_FIELDS + OBJECT_REF + ARRAY_HEADER

    }


    /**
     * Estimates the size of a string in bytes.
     *
     * This function was designed with the following goals in mind (in order of importance):
     *
     * First goal is speed: this function is called repeatedly and it should execute in not much
     * more than a nanosecond.
     *
     * Second goal is to never underestimate as it would lead to a memory shortage and crash.
     *
     * Third goal is to never overestimate too much, say within a factor of 2, as it would
     * mean that we are leaving much of the RAM underutilized.
     */
    fun estimatedSizeOf(input: String): Long {
        return (input.length * 2 + OBJECT_OVERHEAD).toLong()
    }


}
