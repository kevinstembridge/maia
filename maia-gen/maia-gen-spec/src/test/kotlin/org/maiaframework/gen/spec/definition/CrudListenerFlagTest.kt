package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class CrudListenerFlagTest {


    private class ListenerSpec : AbstractSpec(AppKey("Test")) {

        val withListener = entity("com.example", "WithListener") {
            field("name", FieldTypes.string)
            crud {}
        }

        val withoutListener = entity("com.example", "WithoutListener") {
            field("name", FieldTypes.string)
            crud {
                withCrudListener(false)
            }
        }

        val explicitlyWithListener = entity("com.example", "ExplicitlyWithListener") {
            field("name", FieldTypes.string)
            crud {
                withCrudListener(true)
            }
        }

        val noCrud = entity("com.example", "NoCrud") {
            field("name", FieldTypes.string)
        }

        val child = entity("com.example", "Child") {
            field("name", FieldTypes.string)
            foreignKey("parent", withoutListener)
            crud {}
        }

        val childEsDto = searchableDto(baseName = "ChildEs", entityDef = child) {
            elasticsearchEnabled()
            field("name")
            field("parentName", "parent.name")
        }

    }


    private val spec = ListenerSpec()


    @Test
    fun `declaring crud generates the listener by default`() {

        assertThat(spec.withListener.crudDef.withCrudListener.value).isTrue()

    }


    @Test
    fun `withCrudListener(false) turns it off and withCrudListener(true) keeps it`() {

        assertThat(spec.withoutListener.crudDef.withCrudListener.value).isFalse()
        assertThat(spec.explicitlyWithListener.crudDef.withCrudListener.value).isTrue()

    }


    @Test
    fun `turning the listener off leaves the rest of the crud definition in place`() {

        assertThat(spec.withoutListener.crudDef.crudOperationDefs).isNotNull()
        assertThat(spec.noCrud.crudDef.withCrudListener.value).isFalse()

    }


    @Test
    fun `an Elasticsearch-enabled searchable DTO fails when an entity on its path has no listener`() {

        assertThatThrownBy { spec.childEsDto.esRefreshDependencies }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("WithoutListener")
            .hasMessageContaining("CrudListener")

    }


    @Test
    fun `a typeahead over an entity with the listener turned off fails`() {

        assertThatThrownBy {
            object : AbstractSpec(AppKey("Test")) {

                val entityDef = entity("com.example", "Quiet") {
                    field("name", FieldTypes.string) {
                        fieldDisplayName("Name")
                        editableByUser()
                    }
                    crud {
                        create { api {} }
                        withCrudListener(false)
                    }
                }

                val typeaheadDef = typeahead(
                    "com.example",
                    "Quiet",
                    entityDef,
                    sortFieldName = "name",
                    searchTermFieldName = "name"
                ) {
                    idFieldFromEntity(
                        dtoFieldName = "id",
                        entityFieldName = "id",
                        esDocMappingType = EsDocMappingTypes.keyword
                    )
                    fieldFromEntity(
                        dtoFieldName = "name",
                        entityFieldName = "name",
                        esDocMappingType = EsDocMappingTypes.searchAsYouType
                    )
                }

            }
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Quiet")
            .hasMessageContaining("withCrudListener(false)")

    }


}
