package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.nio.file.Path
import kotlin.io.path.readText


class EnumsAndValueTypesPagesTest {


    class TypesModelSpec : AbstractSpec(AppKey("EnumsAndValueTypesPagesTest")) {

        val colour = enumDef("com.example", "Colour") {
            value("RED") { displayName = "Bright Red" }
            value("BLUE")
        }

        val sku = stringType("com.example", "Sku") { alwaysUpperCase() }

        val quantity = intType("com.example", "Quantity")

        val serial = longType("com.example", "Serial")

        val active = booleanType("com.example", "ActiveFlag")

        val widget = entity("com.example", "Widget") {
            field("colour", FieldTypes.enum(colour))
            field("palette", FieldTypes.list(FieldTypes.enum(colour)))
            field("sku", FieldTypes.stringType(sku))
            field("quantity", FieldTypes.intType(quantity))
            field("serial", FieldTypes.longType(serial))
            field("active", FieldTypes.booleanType(active))
        }

        val gadget = entity("com.example", "Gadget") {
            field("colour", FieldTypes.enum(colour))
        }

    }


    class TypesApplicationSpec : ApplicationSpec("com.example") {

        override val modelDefs: List<ModelDef> = listOf(TypesModelSpec().modelDef)

    }


    @TempDir
    lateinit var outputDir: Path


    @Test
    fun `enums page lists values, display names, spec and used-by fields`() {

        generateDomainDocs(listOf(TypesApplicationSpec()), outputDir)

        val page = outputDir.resolve("enums.html").readText()
        assertThat(page)
            .contains("""<a href="index.html">Index</a>""")
            .contains("Colour")
            .contains("TypesApplicationSpec")
            .contains("<td>RED</td>")
            .contains("<td>Bright Red</td>")
            .contains("<td>BLUE</td>")
            .contains("""<a href="entities/Widget.html">Widget</a>.colour""")
            .contains("""<a href="entities/Widget.html">Widget</a>.palette""")
            .contains("""<a href="entities/Gadget.html">Gadget</a>.colour""")
            .doesNotContain("<script")

    }


    @Test
    fun `value types page lists each kind with what the type def carries and used-by fields`() {

        generateDomainDocs(listOf(TypesApplicationSpec()), outputDir)

        val page = outputDir.resolve("value-types.html").readText()
        assertThat(page)
            .contains("""<a href="index.html">Index</a>""")
            .contains("Sku").contains("Quantity").contains("Serial").contains("ActiveFlag")
            .contains("TypesApplicationSpec")
            .contains("ALWAYS_UPPER")
            .contains("""<a href="entities/Widget.html">Widget</a>.sku""")
            .contains("""<a href="entities/Widget.html">Widget</a>.quantity""")
            .contains("""<a href="entities/Widget.html">Widget</a>.serial""")
            .contains("""<a href="entities/Widget.html">Widget</a>.active""")
            .doesNotContain("<script")
        assertThat(page).containsPattern("(?s)Sku.*string")
        assertThat(page).containsPattern("(?s)Quantity.*int")
        assertThat(page).containsPattern("(?s)Serial.*long")
        assertThat(page).containsPattern("(?s)ActiveFlag.*boolean")

    }


    @Test
    fun `Entity field tables link to the enum or value type they use`() {

        generateDomainDocs(listOf(TypesApplicationSpec()), outputDir)

        val page = outputDir.resolve("entities/Widget.html").readText()
        assertThat(page)
            .contains("""<a href="../enums.html#com.example.Colour">Colour</a>""")
            .contains("""<a href="../value-types.html#com.example.Sku">Sku</a>""")
            .contains("""<a href="../value-types.html#com.example.Quantity">Quantity</a>""")
            .contains("""<a href="../value-types.html#com.example.Serial">Serial</a>""")
            .contains("""<a href="../value-types.html#com.example.ActiveFlag">ActiveFlag</a>""")

        assertThat(outputDir.resolve("enums.html").readText()).contains("""id="com.example.Colour"""")
        assertThat(outputDir.resolve("value-types.html").readText()).contains("""id="com.example.Sku"""")

    }


    @Test
    fun `every page links to the enums and value types pages`() {

        generateDomainDocs(listOf(TypesApplicationSpec()), outputDir)

        assertThat(outputDir.resolve("index.html").readText())
            .contains("""<a href="enums.html">Enums</a>""")
            .contains("""<a href="value-types.html">Value types</a>""")
        assertThat(outputDir.resolve("entities/Widget.html").readText())
            .contains("""<a href="../enums.html">Enums</a>""")
            .contains("""<a href="../value-types.html">Value types</a>""")

    }


}
