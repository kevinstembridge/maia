package org.maiaframework.gen.renderers.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File

class TimelineBlotterRowDtoTypescriptRendererTest {


    @Test
    fun `renders import for a response DTO typed field`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val addressDto = simpleResponseDto("com.example", "Address") {
                field("street", FieldTypes.string)
            }

            val widget = entity(
                "com.example",
                "Widget",
                recordVersionHistory = true,
                nameFieldForPkAndNameDto = "name"
            ) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
                field("address", addressDto) { fieldDisplayName("Address") }
            }

            val blotter = timelineBlotter(
                entityDef = widget,
                joinDefs = emptyList()
            ) { }

        }

        TimelineBlotterRowDtoTypescriptRenderer(spec.blotter).renderToDir(tempDir)

        val renderedFile = tempDir.walkTopDown().single { it.extension == "ts" }
        val content = renderedFile.readText()

        val fieldTypeName = Regex("""address\??:\s*(\w+);""").find(content)!!.groupValues[1]

        assertThat(content).contains("import {$fieldTypeName} from")

    }

}
