package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes


class SearchableDtoEsDocTest {


    private class EsDocTestSpec : AbstractSpec(AppKey("Test")) {

        val other = entity("com.example", "Other") {
            field("label", FieldTypes.string)
        }

        val unmappableEntity = entity("com.example", "Unmappable") {
            field("tags", FieldTypes.list(FieldTypes.string))
        }

        val color = enumDef("com.example", "Color")

        val thing = entity("com.example", "Thing") {
            field("color", FieldTypes.enum(color))
            field("score", FieldTypes.double)
            field("name", FieldTypes.string)
            field("count", FieldTypes.int)
            field("active", FieldTypes.boolean)
            field("happenedAt", FieldTypes.instant)
            field("birthday", FieldTypes.localDate)
            field("big", FieldTypes.long)
            foreignKey("other", other)
        }

        val plain = searchableDto(entityDef = thing) {
            field("name")
            field("count")
            field("otherLabel", "other.label")
        }

        val enabled = searchableDto(baseName = "ThingEs", entityDef = thing) {
            elasticsearchEnabled()
            field("name")
            field("count")
            field("color")
            field("score")
            field("other")
            field("active")
            field("happenedAt")
            field("birthday")
            field("big")
            field("otherLabel", "other.label")
        }

        val noAutoRefresh = searchableDto(baseName = "ThingNoRefresh", entityDef = thing) {
            elasticsearchEnabled(autoRefresh = false)
            field("name")
        }

        val twoVersions = searchableDto(baseName = "ThingTwo", entityDef = thing) {
            elasticsearchEnabled(version = 2, autoRefresh = false)
            elasticsearchEnabled(version = 1)
            field("name")
        }

        fun duplicate() = searchableDto(baseName = "ThingDup", entityDef = thing) {
            elasticsearchEnabled()
            elasticsearchEnabled()
        }

        fun unmappable() = searchableDto(baseName = "ThingUnmappable", entityDef = unmappableEntity) {
            elasticsearchEnabled()
            field("tags")
        }

        fun overridden() = searchableDto(baseName = "ThingOverridden", entityDef = unmappableEntity) {
            elasticsearchEnabled()
            field("tags") { esMapping(EsDocMappingTypes.keyword) }
        }

        val compositeEntity = entity("com.example", "Composite") {
            field("a", FieldTypes.string) { primaryKey() }
            field("b", FieldTypes.int) { primaryKey() }
        }

        fun composite() = searchableDto(entityDef = compositeEntity) {
            elasticsearchEnabled()
        }

        val stringKeyEntity = entity("com.example", "StringKey") {
            field("code", FieldTypes.string) { primaryKey() }
            field("label", FieldTypes.string)
        }

        fun stringKey() = searchableDto(baseName = "StringKeyEs", entityDef = stringKeyEntity) {
            elasticsearchEnabled()
        }

        val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
        }

        val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name") {
            field("name", FieldTypes.string)
        }

        val join = simpleManyToManyEntity(
            "com.example",
            "LeftRight",
            leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
            rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
        )

        val m2m = searchableDto(entityDef = left, baseName = "LeftEs") {
            elasticsearchEnabled()
            field("name")
            manyToManyField("rights", join)
        }

    }


    private val spec = EsDocTestSpec()


    private fun EsDocDef.mappingOf(name: String): EsDocMappingType =
        this.fields.single { it.classFieldDef.classFieldName.value == name }.mappingType


    @Test
    fun `elasticsearchEnabled derives one EsDocDef at version 1`() {

        val dto = spec.enabled
        assertThat(dto.esDocDefs).hasSize(1)

        val esDoc = dto.esDocDef!!
        assertThat(esDoc.esDocVersion).isEqualTo(1)
        assertThat(esDoc.packageName).isEqualTo(dto.packageName)
        assertThat(esDoc.esDocBaseName).isEqualTo(dto.dtoBaseName)
        assertThat(esDoc.elasticIndexBaseName.value).isEqualTo("thing_es")
        assertThat(esDoc.generateRefreshIndexJob).isTrue()
        assertThat(esDoc.entityDef).isSameAs(spec.thing)

    }


    @Test
    fun `EsDoc fields mirror the dto fields and exactly one is the id`() {

        val dto = spec.enabled
        val esDoc = dto.esDocDef!!

        assertThat(esDoc.fields.map { it.classFieldDef.classFieldName })
            .containsExactlyInAnyOrderElementsOf(dto.allFieldsSorted.map { it.classFieldName })

        val idFields = esDoc.fields.filter { it.isIdField }
        assertThat(idFields).hasSize(1)
        assertThat(idFields.single().classFieldDef.classFieldName).isEqualTo(dto.idField!!.classFieldName)

    }


    @Test
    fun `default mappings are chosen by field type`() {

        val mappings = spec.enabled.esDocDef!!.fields.associate { it.classFieldDef.classFieldName.value to it.mappingType }

        assertThat(mappings["name"]).isSameAs(EsDocMappingTypes.textAndKeyword)
        assertThat(mappings["count"]).isSameAs(EsDocMappingTypes.long)
        assertThat(mappings["active"]).isSameAs(EsDocMappingTypes.boolean)
        assertThat(mappings["happenedAt"]).isSameAs(EsDocMappingTypes.date)
        assertThat(mappings["birthday"]).isSameAs(EsDocMappingTypes.date)
        assertThat(mappings["big"]).isSameAs(EsDocMappingTypes.long)
        assertThat(mappings["color"]).isSameAs(EsDocMappingTypes.keyword)
        assertThat(mappings["score"]).isSameAs(EsDocMappingTypes.double)
        assertThat(mappings["other"]).isSameAs(EsDocMappingTypes.keyword)
        assertThat(mappings["id"]).isSameAs(EsDocMappingTypes.keyword)
        assertThat(mappings["otherLabel"]).isSameAs(EsDocMappingTypes.textAndKeyword)

    }


    @Test
    fun `many to many field maps to object`() {

        val field = spec.m2m.esDocDef!!.fields.single { it.classFieldDef.classFieldName.value == "rights" }
        assertThat(spec.m2m.esDocDef!!.mappingOf("rights")).isSameAs(EsDocMappingTypes.`object`)
        assertThat(field.entityFieldDef).isNull()

    }


    @Test
    fun `elasticsearchEnabled forces generateFindById`() {

        assertThat(spec.enabled.generateFindById.value).isTrue()

    }


    @Test
    fun `composite primary key root entity is rejected`() {

        assertThatThrownBy { spec.composite() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("single DomainId primary key")

    }


    @Test
    fun `non DomainId primary key root entity is rejected`() {

        assertThatThrownBy { spec.stringKey() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("single DomainId primary key named 'id'")
            .hasMessageContaining("StringKey")

    }


    @Test
    fun `unmappable field type without an override is rejected`() {

        assertThatThrownBy { spec.unmappable() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("tags")

    }


    @Test
    fun `unmappable field type is accepted with an esMapping override`() {

        assertThat(spec.overridden().esDocDef!!.mappingOf("tags")).isSameAs(EsDocMappingTypes.keyword)

    }


    @Test
    fun `esMapping override survives copyWithFieldName`() {

        val field = spec.overridden().findSearchableDtoFieldByName("tags") as SimpleSearchableDtoFieldDef
        val copy = field.copyWithFieldName("renamedTags")

        assertThat(copy.esMappingOverride).isSameAs(EsDocMappingTypes.keyword)

    }


    @Test
    fun `multiple versions yield multiple EsDocDefs and the latest is esDocDef`() {

        assertThat(spec.twoVersions.esDocDefs.map { it.esDocVersion }).containsExactly(2, 1)
        assertThat(spec.twoVersions.esDocDef!!.esDocVersion).isEqualTo(2)

    }


    @Test
    fun `autoRefresh is recorded and defaults to true`() {

        assertThat(spec.noAutoRefresh.esVersions.single().autoRefresh).isFalse()
        assertThat(spec.enabled.esVersions.single().autoRefresh).isTrue()

    }


    @Test
    fun `duplicate version is rejected`() {

        assertThatThrownBy { spec.duplicate() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("already declared")

    }


    @Test
    fun `searchableDto without elasticsearchEnabled has no EsDocs`() {

        assertThat(spec.plain.esDocDefs).isEmpty()
        assertThat(spec.plain.esDocDef).isNull()
        assertThat(spec.plain.generateFindById.value).isFalse()

    }


}
