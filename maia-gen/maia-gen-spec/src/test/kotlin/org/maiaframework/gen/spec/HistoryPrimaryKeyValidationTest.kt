package org.maiaframework.gen.spec

import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDefinitionException
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class HistoryPrimaryKeyValidationTest {


    @Test
    fun `entity with history and a non-surrogate primary key throws ModelDefinitionException`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                field("name", FieldTypes.string) {
                    fieldDisplayName("Name")
                    primaryKey()
                }
            }

        }

        assertThatThrownBy { spec.modelDef }
            .isInstanceOf(ModelDefinitionException::class.java)
            .hasMessageContaining("WithHistory")
            .hasMessageContaining("name")
            .hasMessageContaining("surrogate")

    }


    @Test
    fun `entity with history and the default surrogate primary key is valid`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

        }

        assertThatNoException().isThrownBy { spec.modelDef }

    }


    @Test
    fun `entity without history and a non-surrogate primary key is valid`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val noHistory = entity("com.example", "NoHistory") {
                field("name", FieldTypes.string) {
                    fieldDisplayName("Name")
                    primaryKey()
                }
            }

        }

        assertThatNoException().isThrownBy { spec.modelDef }

    }


}
