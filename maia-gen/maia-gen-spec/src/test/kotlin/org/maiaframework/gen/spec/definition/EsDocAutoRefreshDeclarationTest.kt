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


    private class PartySourcesSpec : AbstractSpec(AppKey("Test")) {

        val party = entity("com.example", "Party") {
            isAbstract = true
            field("displayName", FieldTypes.string)
            crud {}
        }

        val person = entity("com.example", "Person") {
            superclass(party)
            typeDiscriminator("PERSON")
            field("firstName", FieldTypes.string)
            crud {}
        }

        val partyEmail = entity("com.example", "PartyEmail") {
            foreignKey("party", party)
            field("email", FieldTypes.string)
            crud {}
        }

        val partyEsDoc = esDoc("com.example", "Party", indexDescription = "manual") {
            autoRefresh(true)
            sourceEntity(party)
            watchesSubclass(person, "firstName")
            watchesChild(partyEmail, "party")
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class SourcesWithoutAutoRefreshSpec : AbstractSpec(AppKey("Test")) {

        val party = entity("com.example", "Party") {
            field("displayName", FieldTypes.string)
            crud {}
        }

        val partyEsDoc = esDoc("com.example", "Party", indexDescription = "manual") {
            autoRefresh(false)
            sourceEntity(party)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class BadForeignKeySpec : AbstractSpec(AppKey("Test")) {

        val party = entity("com.example", "Party") {
            field("displayName", FieldTypes.string)
            crud {}
        }

        val other = entity("com.example", "Other") {
            field("name", FieldTypes.string)
            crud {}
        }

        val child = entity("com.example", "Child") {
            foreignKey("other", other)
            crud {}
        }

        val partyEsDoc = esDoc("com.example", "Party", indexDescription = "manual") {
            autoRefresh(true)
            sourceEntity(party)
            watchesChild(child, "other")
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class NoListenerSourceSpec : AbstractSpec(AppKey("Test")) {

        val plain = entity("com.example", "Plain") {
            field("name", FieldTypes.string)
        }

        val plainEsDoc = esDoc("com.example", "Plain", indexDescription = "manual") {
            autoRefresh(true)
            sourceEntity(plain)
            field("id", FieldTypes.string) { isIdField() }
        }

    }


    private class UnknownWatchedFieldSpec : AbstractSpec(AppKey("Test")) {

        val party = entity("com.example", "Party") {
            field("displayName", FieldTypes.string)
            crud {}
        }

        val partyEsDoc = esDoc("com.example", "Party", indexDescription = "manual") {
            autoRefresh(true)
            sourceEntity(party, "nope")
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
    fun `autoRefresh true without a source entity is rejected for a hand-built EsDoc`() {

        assertThatThrownBy { AutoRefreshTrueSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("requires sourceEntity")
            .hasMessageContaining("com.example.WantsAuto V1")

    }


    @Test
    fun `sources are rejected without autoRefresh true`() {

        assertThatThrownBy { SourcesWithoutAutoRefreshSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("not autoRefresh(true)")

    }


    @Test
    fun `declared sources become refresh dependencies`() {

        val spec = PartySourcesSpec()
        val dependencies = spec.partyEsDoc.refreshDependencies

        assertThat(dependencies.map { it.entityDef.entityBaseName.value to it.role })
            .containsExactly(
                "Party" to EsRefreshRole.ROOT,
                "Person" to EsRefreshRole.ROOT,
                "PartyEmail" to EsRefreshRole.CHILD
            )

        val party = dependencies[0]
        assertThat(party.refreshOnAnyUpdate).isTrue()

        val person = dependencies[1]
        assertThat(person.watchedFields.map { it.value }).containsExactly("firstName")
        assertThat(person.refreshOnAnyUpdate).isFalse()

        val email = dependencies[2]
        assertThat(email.watchedFields.map { it.value }).containsExactlyInAnyOrder("email", "party")
        assertThat(email.pathsFromRoot.single().single().toField.classFieldName.value).isEqualTo("party")

    }


    @Test
    fun `a child that is not linked to the source entity by a foreign key is rejected`() {

        assertThatThrownBy { BadForeignKeySpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("must be a foreign key to Party")

    }


    @Test
    fun `a source entity without a CrudListener is rejected`() {

        assertThatThrownBy { NoListenerSourceSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("requires entity Plain to have a CrudListener")

    }


    @Test
    fun `a watched field that does not exist is rejected`() {

        assertThatThrownBy { UnknownWatchedFieldSpec() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("has no field 'nope' to watch")

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
