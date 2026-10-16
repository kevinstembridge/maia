package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.singleKotlinFileIn
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class EsDocFieldNameMapperRendererTest {


    private class BlotterSpec : AbstractSpec(AppKey("Test")) {

        val widget = entity("com.example", "Widget") {
            field("title", FieldTypes.string)
            field("quantity", FieldTypes.int)
            crud {}
        }

        val widgetEsDto = searchableDto(baseName = "WidgetEs", entityDef = widget) {
            elasticsearchEnabled()
            field("title")
            field("quantity")
            field("createdTimestamp")
        }

        val widgetEsBlotter = blotter(widgetEsDto) {
            columnFromDto("id")
            columnFromDto("title")
            columnFromDto("quantity")
            columnFromDto("createdTimestamp")
        }

        val manualEsDoc = esDoc("com.example", "Manual", indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.domainId) { isIdField() }
            field("code", FieldTypes.string) { mappingType(EsDocMappingTypes.keyword) }
            field("description", FieldTypes.string) { mappingType(EsDocMappingTypes.text) }
            field("label", FieldTypes.string) { mappingType(EsDocMappingTypes.textAndKeyword) }
        }

        val manualBlotter = blotter(manualEsDoc) {
            columnFromDto("id")
            columnFromDto("code")
            columnFromDto("description")
            columnFromDto("label", dtoFieldName = "theLabel")
        }

    }


    private val spec = BlotterSpec()


    @Test
    fun `ES-enabled searchable DTO blotter filters and sorts textAndKeyword string columns on the raw keyword sub-field`(@TempDir dir: File) {

        assertThat(spec.widgetEsBlotter.blotterSourceDef).isInstanceOf(BlotterEsDocSourceDef::class.java)

        EsDocFieldNameMapperRenderer(spec.widgetEsBlotter).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        assertThat(source).doesNotContain("sortMappingFunction")
        assertThat(source).contains("\"title\" -> \"title.raw\"")
        assertThat(source).contains("\"quantity\" -> \"quantity\"")
        assertThat(source).contains("\"createdTimestamp\" -> \"createdTimestamp\"")
        assertThat(source).contains("\"id\" -> \"id\"")
        assertThat(source).doesNotContain("quantity.raw", "createdTimestamp.raw", "id.raw")

    }


    @Test
    fun `manual EsDoc blotter only maps textAndKeyword columns to raw`(@TempDir dir: File) {

        EsDocFieldNameMapperRenderer(spec.manualBlotter).renderToDir(dir)
        val source = singleKotlinFileIn(dir)

        assertThat(source).contains("\"theLabel\" -> \"label.raw\"")
        assertThat(source).contains("\"code\" -> \"code\"")
        assertThat(source).contains("\"description\" -> \"description\"")
        assertThat(source).contains("\"id\" -> \"id\"")

    }


}
