package org.maiaframework.gen.spec

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDefinitionException
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class HistoryFkValidationTest {


    @Test
    fun `regular entity with history table FKing a non-history entity throws ModelDefinitionException`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val noHistory = entity("com.example", "NoHistory", deletable = Deletable.TRUE) {
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

            val noHistory = entity("com.example", "NoHistory", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
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
    fun `non-history entity FKing a non-history entity is valid`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val child = entity("com.example", "Child") {
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        assertThatNoException().isThrownBy { spec.modelDef }

    }


    @Test
    fun `history entity FKing a non-deletable non-history entity is valid`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val nonDeletable = entity("com.example", "NonDeletable") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                foreignKey("nonDeletable", nonDeletable) { fieldDisplayName("Non-Deletable") }
            }

        }

        assertThatNoException().isThrownBy { spec.modelDef }

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


    @Test
    fun `history entity FKing another history entity gets a synthesized version-tracking field`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        val childHistoryFields = spec.child.historyEntityDef!!.allEntityFieldsSorted
        val parentVersionField = childHistoryFields.single { it.classFieldName.value == "parentVersion" }

        assertThat(parentVersionField.isPrimaryKey.value).isFalse()
        assertThat(parentVersionField.nullable).isFalse()
        assertThat(parentVersionField.tableColumnName.value).isEqualTo("parent_version")

    }


    @Test
    fun `history entity FKing a non-history entity gets no synthesized version-tracking field`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val nonDeletable = entity("com.example", "NonDeletable") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                foreignKey("nonDeletable", nonDeletable) { fieldDisplayName("Non-Deletable") }
            }

        }

        val historyFields = spec.withHistory.historyEntityDef!!.allEntityFieldsSorted

        assertThat(historyFields.map { it.classFieldName.value }).doesNotContain("nonDeletableVersion")

    }


    @Test
    fun `a subclass entity FKing another history entity gets a synthesized version-tracking field on its own history entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val vehicle = entity("com.example", "Vehicle", recordVersionHistory = true) {
                typeDiscriminator("VEHICLE")
            }

            val car = entity("com.example", "Car", recordVersionHistory = true) {
                superclass(vehicle)
                typeDiscriminator("CAR")
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        val carHistoryFields = spec.car.historyEntityDef!!.allEntityFieldsSorted
        val parentVersionField = carHistoryFields.single { it.classFieldName.value == "parentVersion" }

        assertThat(parentVersionField.isPrimaryKey.value).isFalse()
        assertThat(parentVersionField.nullable).isFalse()

    }

}
