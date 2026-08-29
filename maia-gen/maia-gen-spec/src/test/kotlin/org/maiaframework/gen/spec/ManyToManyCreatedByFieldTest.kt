package org.maiaframework.gen.spec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class ManyToManyCreatedByFieldTest {


    @Test
    fun `simpleManyToManyEntity supports field_createdById`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val user = entity("com.example", "User") {
                field("username", FieldTypes.string) { fieldDisplayName("Username") }
            }

            val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val join = simpleManyToManyEntity(
                "com.example",
                "Join",
                leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
                rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
            ) {
                field_createdById(user)
            }

        }

        assertThat(spec.join.entityDef.hasCreatedByIdField).isTrue()

    }

}
