package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


class HazelcastEntityConfigRenderer(
    private val cacheableEntityDefs: List<EntityDef>,
    hazelcastEntityConfigClassDef: ClassDef
) : AbstractKotlinRenderer(
    hazelcastEntityConfigClassDef
) {


    init {

        cacheableEntityDefs.forEach {

            val classFieldDef = aClassField(
                it.hazelcastSerializerClassDef.uqcn.firstToLower(),
                it.hazelcastSerializerClassDef.fqcn
            ).privat().build()

            addConstructorArg(classFieldDef)

            if (it.hasCompositePrimaryKey) {

                val classFieldDef = aClassField(
                    it.hazelcastPrimaryKeySerializerClassDef.uqcn.firstToLower(),
                    it.hazelcastPrimaryKeySerializerClassDef.fqcn
                ).privat().build()

                addConstructorArg(classFieldDef)

            }

        }

    }


    override fun renderPreClassFields() {

        addImportFor(Fqcns.HAZELCAST_MAP_CONFIG)
        addImportFor(Fqcns.HAZELCAST_COMPACT_SERIALIZER)

        append("""
            |
            |
            |    val serializers: List<CompactSerializer<out Any>> = listOf(
            |""".trimMargin()
        )

        cacheableEntityDefs.forEach {

            addImportFor(it.entityFqcn)
            appendLine("        ${it.hazelcastSerializerClassDef.uqcn.firstToLower()},")

            if (it.hasCompositePrimaryKey) {
                addImportFor(it.entityPkClassDef.fqcn)
                appendLine("        ${it.hazelcastPrimaryKeySerializerClassDef.uqcn.firstToLower()},")
            }

        }

        append("""
            |    )
            |
            |
            |    val mapConfigs: List<MapConfig> = listOf(
            |""".trimMargin()
        )

        // TODO render MapConfig for each cache that defines it

        appendLine("    )")

    }


}
