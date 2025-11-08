package org.maiaframework.gen.spec.definition.lang


import org.maiaframework.common.BlankStringException
import java.util.Objects

class Fqcn private constructor(private val rawFqcn: String) : Comparable<Fqcn> {

    val packageName: PackageName

    val uqcn: Uqcn

    val isInLangPackage: Boolean = this.rawFqcn.startsWith("java.lang") || this.rawFqcn.startsWith("kotlin.")


    init {

        BlankStringException.throwIfBlank(rawFqcn, "rawFqcn")
        this.uqcn = initUqcn(rawFqcn)
        this.packageName = initPackageNameFrom(rawFqcn)

    }


    private fun initUqcn(rawFqcn: String): Uqcn {

        val lastIndexOfDot = rawFqcn.lastIndexOf(".")

        if (lastIndexOfDot == -1) {
            throw RuntimeException("Please don't put classes in the default package. Provided FQCN = [$rawFqcn]")
        }

        val rawUqcn = rawFqcn.substring(lastIndexOfDot + 1)
        return Uqcn(rawUqcn)

    }


    private fun initPackageNameFrom(rawFqcn: String): PackageName {

        val lastIndexOfDot = rawFqcn.lastIndexOf(".")

        if (lastIndexOfDot == -1) {
            throw RuntimeException("Please don't put classes in the default package. Provided FQCN = [$rawFqcn]")
        }

        val rawPackageName = rawFqcn.substring(0, lastIndexOfDot)
        return PackageName(rawPackageName)

    }


    val unqualifiedToString: String = this.uqcn.value


    override fun equals(other: Any?): Boolean {

        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val fqcn = other as Fqcn?

        return this.rawFqcn == fqcn!!.rawFqcn

    }

    override fun hashCode(): Int {

        return Objects.hash(this.rawFqcn)

    }


    override fun toString(): String {

        return this.rawFqcn

    }


    fun notInSamePackageAs(otherFqcn: Fqcn): Boolean {

        return this.packageName == otherFqcn.packageName == false

    }


    fun withSuffix(suffix: String): Fqcn {

        return valueOf(this.rawFqcn + suffix)

    }


    fun withPrefix(prefix: String): Fqcn {

        return valueOf(this.packageName, this.uqcn.withPrefix(prefix))

    }


    override fun compareTo(other: Fqcn): Int {

        return this.rawFqcn.compareTo(other.rawFqcn)

    }


    companion object {

        val ANY = valueOf("kotlin.Any")
        val BOOLEAN = valueOf("kotlin.Boolean")
        val BYTE = valueOf("kotlin.Byte")
        val DOUBLE = valueOf("kotlin.Double")
        val FLOAT = valueOf("kotlin.Float")
        val INSTANT = valueOf("java.time.Instant")
        val INT = valueOf("kotlin.Int")
        val LIST = valueOf("kotlin.collections.List")
        val LOCAL_DATE = valueOf("java.time.LocalDate")
        val LONG = valueOf("kotlin.Long")
        val MAP = valueOf("kotlin.collections.Map")
        val PERIOD = valueOf("java.time.Period")
        val SET = valueOf("kotlin.collections.Set")
        val STRING = valueOf("java.lang.String")
        val URL = valueOf("java.net.URL")


        inline fun <reified T> valueOf(): Fqcn = valueOf(T::class.java)


        fun valueOf(clazz: Class<*>): Fqcn {

            return Fqcn(clazz.canonicalName)

        }


        fun valueOf(rawFqcn: String): Fqcn {

            return Fqcn(rawFqcn)

        }

        fun valueOf(packageName: PackageName, uqcn: Uqcn): Fqcn {

            return valueOf(packageName.value + "." + uqcn.value)

        }
    }


}
