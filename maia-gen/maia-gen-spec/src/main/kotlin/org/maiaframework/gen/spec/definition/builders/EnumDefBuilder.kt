package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.DisplayName
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EnumValueDef
import org.maiaframework.gen.spec.definition.lang.Fqcn


@MaiaDslMarker
class EnumDefBuilder(
    private val enumFqcn: Fqcn
) {


    private val enumValueDefs = mutableListOf<EnumValueDefBuilder>()


    private var provided: Boolean = false


    private var withTypescript: Boolean = false


    private var withEnumSelectionOptions: Boolean = false


    fun withTypescript(withEnumSelectionOptions: Boolean = false): EnumDefBuilder {

        this.withTypescript = true
        this.withEnumSelectionOptions = withEnumSelectionOptions
        return this

    }


    fun build(): EnumDef {

        val valueDefs = if (provided && withTypescript && enumValueDefs.isEmpty()) {
            reflectEnumValues()
        } else {
            this.enumValueDefs.map { it.build() }.toList()
        }

        return EnumDef(
            this.enumFqcn,
            valueDefs,
            this.provided,
            this.withTypescript,
            this.withEnumSelectionOptions
        )

    }


    fun provided() {

        this.provided = true

    }


    fun value(
        name: String,
        init: (EnumValueDefBuilder.() -> Unit)? = null
    ) {

        val builder = EnumValueDefBuilder(name)
        this.enumValueDefs.add(builder)
        init?.invoke(builder)

    }


    private fun reflectEnumValues(): List<EnumValueDef> {

        val clazz = try {
            Class.forName(enumFqcn.toString())
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(
                "Cannot find provided enum class $enumFqcn on classpath for TypeScript generation", e
            )
        }

        require(clazz.isEnum) { "$enumFqcn is not an enum class" }

        val displayNameField = try {
            clazz.getDeclaredField("displayName").also { it.isAccessible = true }
        } catch (_: NoSuchFieldException) {
            null
        }

        return clazz.enumConstants.map { constant ->
            val name = (constant as Enum<*>).name
            val displayName = displayNameField?.get(constant)?.toString()
                ?: name.split("_").joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
            EnumValueDef(name = name, displayName = DisplayName(displayName))
        }

    }


}
