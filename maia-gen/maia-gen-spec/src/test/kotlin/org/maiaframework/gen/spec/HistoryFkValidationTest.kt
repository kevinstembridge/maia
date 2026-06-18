package org.maiaframework.gen.spec

import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDefinitionException
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class HistoryFkValidationTest {

    @Test
    fun `regular entity with history table FKing a non-history entity throws ModelDefinitionException`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val noHistory = entity("com.example", "NoHistory") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                foreignKey("noHistory", noHistory) { fieldDisplayName("No History") }
            }

        }

        assertThatThrownBy { spec.modelDef }
            .isInstanceOf(ModelDefinitionException::class.java)
            .hasMessageContaining("WithHistory")
            .hasMessageContaining("NoHistory")

    }


    @Test
    fun `many-to-many join entity with history FKing a non-history entity throws ModelDefinitionException`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val noHistory = entity("com.example", "NoHistory", nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true, nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val join = manyToManyEntity(
                "com.example",
                "Join",
                recordVersionHistory = true,
                leftEntity = ReferencedEntity("left", "Left", noHistory, IsEditableByUser.TRUE),
                rightEntity = ReferencedEntity("right", "Right", withHistory, IsEditableByUser.TRUE)
            )

        }

        assertThatThrownBy { spec.modelDef }
            .isInstanceOf(ModelDefinitionException::class.java)
            .hasMessageContaining("Join")
            .hasMessageContaining("NoHistory")

    }


    @Test
    fun `history entity FKing another history entity is valid`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        assertThatNoException().isThrownBy { spec.modelDef }

    }

}
