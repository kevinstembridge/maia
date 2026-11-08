package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class EsDocBlotterVersioningTest {


    private val spec = object : AbstractSpec(AppKey("Test")) {

        val widgetV2EsDoc = esDoc("com.example", "Widget", esDocVersion = 2, indexDescription = "Widgets") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

        val widgetBlotter = blotter(widgetV2EsDoc) {
            moduleName("ops")
            columnFromDto(fieldPathInSourceData = "id", dtoFieldName = "id") { header("ID") }
        }

    }


    @Test
    fun `names of the blotter stack include the esDoc version`() {

        val blotterDef = spec.widgetBlotter

        assertThat(blotterDef.dtoUqcn.value).isEqualTo("WidgetV2BlotterRowDto")
        assertThat(blotterDef.blotterComponent.componentName).isEqualTo("WidgetV2Blotter")

    }


    @Test
    fun `api urls of the blotter carry the esDoc version as a path segment`() {

        val searchDtoDef = spec.widgetBlotter.searchDtoDef

        assertThat(searchDtoDef.searchApiUrl).isEqualTo("/api/v2/ops/widget-blotter/search")
        assertThat(searchDtoDef.countApiUrl).isEqualTo("/api/v2/ops/widget-blotter/count")

    }


}
