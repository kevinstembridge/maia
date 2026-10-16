package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class EsDocAutoRefreshDeclarationTest {


    private class UndeclaredSpec : AbstractSpec(AppKey("Test")) {

        val manual = esDoc("com.example", "Undeclared", indexDescription = "manual") {
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class AutoRefreshTrueSpec : AbstractSpec(AppKey("Test")) {

        val manual = esDoc("com.example", "WantsAuto", indexDescription = "manual") {
            autoRefresh(true)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class AutoRefreshFalseSpec : AbstractSpec(AppKey("Test")) {

        val manual = esDoc("com.example", "SelfRefreshed", esDocVersion = 2, indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class CoexistSpec : AbstractSpec(AppKey("Test")) {

        val thing = entity("com.example", "Thing") {
            field("name", FieldTypes.string)
        }

        val dto = searchableDto(baseName = "ThingEs", entityDef = thing) {
            elasticsearchEnabled()
            field("name")
        }

        val manual = esDoc("com.example", "ThingManual", indexDescription = "manual") {
            autoRefresh(false)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    @Test
    fun `undeclared refresh strategy is rejected naming the EsDoc`() {

        assertThatThrownBy { UndeclaredSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("com.example.Undeclared V1")
            .hasMessageContaining("autoRefresh(false)")

    }


    @Test
    fun `autoRefresh true is rejected for a hand-built EsDoc`() {

        assertThatThrownBy { AutoRefreshTrueSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("only supported for elasticsearchEnabled")
            .hasMessageContaining("com.example.WantsAuto V1")

    }


    @Test
    fun `autoRefresh false is accepted and the EsDoc is registered`() {

        val spec = AutoRefreshFalseSpec()

        assertThat(spec.modelDef.esDocsDefs).containsExactly(spec.manual)

    }


    @Test
    fun `hand-built EsDoc coexists with an elasticsearchEnabled searchable dto`() {

        val spec = CoexistSpec()

        assertThat(spec.modelDef.esDocsDefs).contains(spec.manual)
        assertThat(spec.modelDef.esDocsDefs).containsAll(spec.dto.esDocDefs)
        assertThat(spec.modelDef.esDocsDefs).hasSize(spec.dto.esDocDefs.size + 1)

    }


}
