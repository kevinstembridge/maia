package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.EnumDef
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

        return EnumDef(
            this.enumFqcn,
            this.enumValueDefs.map { it.build() }.toList(),
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

}
