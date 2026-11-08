package org.maiaframework.gen.renderers.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class BlotterRoutesRendererTest {


    @Test
    fun `renders a lazy route to the blotter page of a blotter that has no entity`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val widgetEsDoc = esDoc("com.example", "Widget", indexDescription = "Widgets") {
                autoRefresh(false)
                field("id", FieldTypes.string) { isIdField() }
            }

            val widgetBlotter = blotter(widgetEsDoc) {
                moduleName("ops")
                columnFromDto(fieldPathInSourceData = "id", dtoFieldName = "id") { header("ID") }
            }

            val widgetBlotterPage = blotterPage(widgetBlotter)

        }

        BlotterRoutesRenderer(spec.widgetBlotterPage).renderToDir(tempDir)

        val routes = tempDir.walkTopDown().single { it.name == "widget-v1-blotter-routes.ts" }.readText()

        assertThat(routes)
            .contains("export const widgetV1BlotterRoutes: Routes = [")
            .contains("path: 'ops/widget-v1-blotter',")
            .contains("import('./widget-v1-blotter-page').then(m => m.WidgetV1BlotterPage)")
            .doesNotContain("authorities")

    }


}
