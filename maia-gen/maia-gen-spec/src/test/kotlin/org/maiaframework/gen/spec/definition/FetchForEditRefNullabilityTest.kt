package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.lang.EntityRefFieldType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability


class FetchForEditRefNullabilityTest {


    private class RefNullabilitySpec : AbstractSpec(AppKey("Test")) {

        val other = entity("com.example", "Other") {
            refDtoFieldNames("label")
            field("label", FieldTypes.string)
        }

        val thing = entity("com.example", "Thing") {
            foreignKey("required", other)
            foreignKey("optional", other) {
                nullable()
            }
        }

    }


    private val spec = RefNullabilitySpec()


    @Test
    fun `a nullable foreign key becomes a nullable RefDto field`() {

        val refField = this.spec.thing.findFieldByName("optional").classFieldDef.asRefDtoClassFieldDef

        assertThat(refField.fieldType).isInstanceOf(EntityRefFieldType::class.java)
        assertThat(refField.nullability).isEqualTo(Nullability.NULLABLE)

    }


    @Test
    fun `a required foreign key becomes a non-null RefDto field`() {

        val refField = this.spec.thing.findFieldByName("required").classFieldDef.asRefDtoClassFieldDef

        assertThat(refField.fieldType).isInstanceOf(EntityRefFieldType::class.java)
        assertThat(refField.nullability).isEqualTo(Nullability.NOT_NULLABLE)

    }


}
