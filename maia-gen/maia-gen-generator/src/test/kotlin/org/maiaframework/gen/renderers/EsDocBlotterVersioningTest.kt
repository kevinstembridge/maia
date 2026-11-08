package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class EsDocBlotterVersioningTest {


    private val spec = object : AbstractSpec(AppKey("Test")) {

        val widgetEsDoc_v0002 = esDoc("com.example", "Widget", esDocVersion = 2, indexDescription = "Widgets") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

        val widgetBlotter = blotter(widgetEsDoc_v0002) {
            moduleName("ops")
            columnFromDto(fieldPathInSourceData = "id", dtoFieldName = "id") { header("ID") }
        }

    }


    @Test
    fun `names of the blotter stack include the esDoc version`() {

        val blotterDef = spec.widgetBlotter

        assertThat(blotterDef.dtoUqcn.value).isEqualTo("WidgetBlotterRowDto_v0002")
        assertThat(blotterDef.blotterComponent.componentName).isEqualTo("WidgetBlotter_v0002")

    }


    @Test
    fun `api urls of the blotter carry the esDoc version as a path segment`() {

        val searchDtoDef = spec.widgetBlotter.searchDtoDef

        assertThat(searchDtoDef.searchApiUrl).isEqualTo("/api/v2/ops/widget-blotter/search")
        assertThat(searchDtoDef.countApiUrl).isEqualTo("/api/v2/ops/widget-blotter/count")

    }


}
