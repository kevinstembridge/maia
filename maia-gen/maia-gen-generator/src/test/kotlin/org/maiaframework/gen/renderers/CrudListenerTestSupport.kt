package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class CrudListenerTestSpec : AbstractSpec(AppKey("Test")) {


    /** Fully CRUD-enabled (and therefore listener-enabled) entity. */
    val widget = entity("com.example", "Widget", deletable = Deletable.TRUE) {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        crud {
            create {
                api {}
            }
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    /** References Widget, so Widget's delete function renders a foreign-key guard. */
    val widgetPart = entity("com.example", "WidgetPart", deletable = Deletable.TRUE) {
        field("label", FieldTypes.string) {
            fieldDisplayName("Label")
        }
        foreignKey("widget", widget)
    }


    /** Listener-enabled but NOT deletable. */
    val sprocket = entity("com.example", "Sprocket") {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        crud {
            create {
                api {}
            }
            update {
                api {}
            }
        }
    }


    /** Modifiable and deletable, but WITHOUT a crud listener. */
    val plain = entity("com.example", "Plain", deletable = Deletable.TRUE) {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
    }


    /** Listener-enabled entity with a composite primary key. */
    val pairing = entity("com.example", "Pairing", deletable = Deletable.TRUE) {
        field("left", FieldTypes.string) {
            fieldDisplayName("Left")
            primaryKey()
        }
        field("right", FieldTypes.int) {
            fieldDisplayName("Right")
            primaryKey()
        }
        field("note", FieldTypes.string) {
            fieldDisplayName("Note")
            editableByUser()
        }
        crud {
            update {
                api {}
            }
            delete {
                api {}
            }
        }
    }


    /** Abstract, listener-enabled superclass. */
    val vehicle = entity("com.example", "Vehicle") {
        isAbstract = true
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        crud {}
    }


    /** Listener-enabled subclass: its notifier must also fan out to Vehicle listeners. */
    val car = entity("com.example", "Car") {
        superclass(vehicle)
        typeDiscriminator("CAR")
        field("doors", FieldTypes.int) {
            fieldDisplayName("Doors")
            editableByUser()
        }
        crud {
            update {
                api {}
            }
        }
    }


    val widgetTypeahead = typeahead("com.example", "Widget", widget, sortFieldName = "name", searchTermFieldName = "name") {
        idFieldFromEntity(dtoFieldName = "id", entityFieldName = "id", esDocMappingType = EsDocMappingTypes.keyword)
        fieldFromEntity(dtoFieldName = "name", entityFieldName = "name", esDocMappingType = EsDocMappingTypes.searchAsYouType)
    }


}


object CrudListenerTestSupport {


    val spec = CrudListenerTestSpec()


    val applicationModelDef: ApplicationModelDef = object : ApplicationSpec("com.example") {
        override val modelDefs: List<ModelDef> = listOf(spec.modelDef)
    }.applicationModelDef


    fun singleKotlinFileIn(dir: File): String {

        val files = dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        assertThat(files).withFailMessage("Expected exactly one rendered .kt file but found: $files").hasSize(1)
        return files.single().readText()

    }


    /** Returns the text of the function starting with [signaturePrefix] up to its closing brace. */
    fun functionBody(source: String, signaturePrefix: String): String {

        assertThat(source).contains(signaturePrefix)
        return signaturePrefix + source.substringAfter(signaturePrefix).substringBefore("\n    }\n")

    }


    /** Asserts that each fragment occurs in [text], in the given order. */
    fun assertInOrder(text: String, vararg fragments: String) {

        var fromIndex = 0

        fragments.forEach { fragment ->
            val index = text.indexOf(fragment, fromIndex)
            assertThat(index)
                .withFailMessage("Expected <%s> after index %s in:\n%s", fragment, fromIndex, text)
                .isGreaterThanOrEqualTo(0)
            fromIndex = index + fragment.length
        }

    }


}
