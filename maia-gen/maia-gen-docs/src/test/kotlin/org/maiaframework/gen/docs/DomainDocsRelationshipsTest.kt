package org.maiaframework.gen.docs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.nio.file.Path
import kotlin.io.path.readText


class DomainDocsRelationshipsTest {


    class LibraryModelSpec : AbstractSpec(AppKey("RelLibrary")) {

        val widget = entity("com.example.lib", "Widget") {
            field("name", FieldTypes.string)
        }

    }


    class LibraryApplicationSpec : ApplicationSpec("com.example.lib") {

        override val modelDefs: List<ModelDef> = listOf(LibraryModelSpec().modelDef)

    }


    class AppModelSpec : AbstractSpec(AppKey("RelApp")) {

        private val widget = LibraryModelSpec().widget

        val animal = entity("com.example.app", "Animal") {
            typeDiscriminator("ANIMAL")
            isAbstract = true
            field("species", FieldTypes.string)
        }

        val dog = entity("com.example.app", "Dog") {
            superclass(animal)
            typeDiscriminator("DOG")
            field("breed", FieldTypes.string)
        }

        val audited = entity("com.example.app", "Audited", recordVersionHistory = true) {
            field("label", FieldTypes.string)
        }

        val gadget = entity("com.example.app", "Gadget") {
            foreignKey("widget", widget)
        }

        val left = entity("com.example.app", "Left") {
            refDtoFieldNames("name")
            field("name", FieldTypes.string)
        }

        val right = entity("com.example.app", "Right") {
            refDtoFieldNames("name")
            field("name", FieldTypes.string)
        }

        val leftToRight = simpleManyToManyEntity(
            "com.example.app",
            "LeftToRight",
            leftEntity = ReferencedEntity("left", "Left", left),
            rightEntity = ReferencedEntity("right", "Right", right)
        )

    }


    class AppApplicationSpec : ApplicationSpec("com.example.app") {

        override val modelDefs: List<ModelDef> = listOf(AppModelSpec().modelDef)

    }


    @TempDir
    lateinit var outputDir: Path


    private fun page(name: String): String {

        generateDomainDocs(listOf(LibraryApplicationSpec(), AppApplicationSpec()), outputDir)
        return outputDir.resolve("entities/$name.html").readText()

    }


    @Test
    fun `superclass page lists subclasses and is flagged abstract with discriminator`() {

        val animal = page("Animal")

        assertThat(animal).containsPattern("Subclasses</dt>\\s*<dd>.*<a href=\"Dog.html\">Dog</a>")
        assertThat(animal).containsPattern("Abstract</dt>\\s*<dd>yes")
        assertThat(animal).containsPattern("Type discriminator</dt>\\s*<dd>ANIMAL")

    }


    @Test
    fun `subclass page links to superclass, shows discriminator and links inherited fields to their parent`() {

        val dog = page("Dog")

        assertThat(dog).containsPattern("Superclass</dt>\\s*<dd><a href=\"Animal.html\">Animal</a>")
        assertThat(dog).containsPattern("Type discriminator</dt>\\s*<dd>DOG")
        assertThat(dog).containsPattern("<td>species</td>.*<a href=\"Animal.html\">Animal</a>")
        assertThat(dog).doesNotContainPattern("<td>breed</td>.*<a href=\"Animal.html\">")

    }


    @Test
    fun `History entity and its source Entity link to each other`() {

        assertThat(page("Audited")).containsPattern("History entity</dt>\\s*<dd><a href=\"AuditedHistory.html\">AuditedHistory</a>")
        assertThat(page("AuditedHistory")).containsPattern("History of</dt>\\s*<dd><a href=\"Audited.html\">Audited</a>")

    }


    @Test
    fun `many-to-many join Entity is flagged and links to both sides`() {

        val join = page("LeftToRight")

        assertThat(join).containsPattern("Many-to-many join</dt>\\s*<dd>yes")
        assertThat(join).containsPattern("Left side</dt>\\s*<dd><a href=\"Left.html\">Left</a>")
        assertThat(join).containsPattern("Right side</dt>\\s*<dd><a href=\"Right.html\">Right</a>")
        assertThat(page("Gadget")).containsPattern("Many-to-many join</dt>\\s*<dd>no")

    }


    @Test
    fun `outgoing and incoming relationships link across Specs`() {

        val gadget = page("Gadget")
        val widget = page("Widget")

        assertThat(gadget.substringAfter("<h2>References</h2>")).contains("<a href=\"Widget.html\">Widget</a>").contains("widget")
        assertThat(widget.substringAfter("<h2>Referenced by</h2>")).contains("<a href=\"Gadget.html\">Gadget</a>")
        assertThat(widget.substringAfter("<h2>Referenced by</h2>")).doesNotContain("Dog")

    }


}
