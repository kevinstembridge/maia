package org.maiaframework.gen.renderers.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AgGridCellRendererDefs
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class AgGridBlotterActionColumnEnabledWhenTest {


    @Test
    fun `action column with enabledWhen passes the row field to the icon renderer and guards the click`(@TempDir tempDir: File) {

        val colDef = renderActionColumn(tempDir) { enabledWhen("eligible") }

        assertThat(colDef)
            .contains("cellRendererParams: { iconName: 'rate_review', enabledWhenField: 'eligible' },")
            .contains("if (event.data?.eligible !== true) {")
            .contains("this.onReview(event.data);")

    }


    @Test
    fun `action column without enabledWhen is rendered exactly as before`(@TempDir tempDir: File) {

        val colDef = renderActionColumn(tempDir) { }

        assertThat(colDef)
            .contains("cellRendererParams: { iconName: 'rate_review' },")
            .contains("this.onReview(event.data);")
            .doesNotContain("enabledWhenField")
            .doesNotContain("!== true")

    }


    private fun renderActionColumn(
        tempDir: File,
        init: org.maiaframework.gen.spec.definition.builders.BlotterActionColumnDefBuilder.() -> Unit
    ): String {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val widgetEsDoc = esDoc("com.example", "Widget", indexDescription = "Widgets") {
                autoRefresh(false)
                field("id", FieldTypes.string) { isIdField() }
                field("eligible", FieldTypes.boolean)
            }

            val widgetBlotter = blotter(widgetEsDoc) {
                moduleName("ops")
                columnFromDto(fieldPathInSourceData = "id", dtoFieldName = "id") { header("ID") }
                actionColumn("review", AgGridCellRendererDefs.iconAction) {
                    header("Review")
                    icon("rate_review")
                    init()
                }
            }

        }

        AgGridBlotterComponentRenderer(spec.widgetBlotter, null, null, null, false, null).renderToDir(tempDir)

        return tempDir.walkTopDown().single { it.name.endsWith("-blotter.ts") }.readText()

    }


}
