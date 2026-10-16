package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.EsDocMappingTypes
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.ReferencedEntity
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File


class SearchableDtoEsTestSpec : AbstractSpec(AppKey("Test")) {


    val parent = entity("com.example", "Parent") {
        field("name", FieldTypes.string)
        crud {}
    }


    val child = entity("com.example", "Child") {
        field("title", FieldTypes.string)
        foreignKey("parent", parent)
        crud {}
    }


    /** Two ES versions of the same searchable DTO. */
    val esDto = searchableDto(baseName = "ChildEs", entityDef = child) {
        elasticsearchEnabled(version = 1)
        elasticsearchEnabled(version = 2)
        field("title")
        field("parentName", "parent.name")
    }


    val plainDto = searchableDto(baseName = "ChildPlain", entityDef = child) {
        field("title")
    }


    /** A typeahead: must keep rendering through the existing typeahead path only. */
    val parentTypeahead = typeahead("com.example", "Parent", parent, sortFieldName = "name", searchTermFieldName = "name") {
        idFieldFromEntity(dtoFieldName = "id", entityFieldName = "id", esDocMappingType = EsDocMappingTypes.keyword)
        fieldFromEntity(dtoFieldName = "name", entityFieldName = "name", esDocMappingType = EsDocMappingTypes.searchAsYouType)
    }


    /** A hand-written (manual) EsDoc: must never get derived control/service/repo classes. */
    val manualEsDoc = esDoc("com.example", "Manual", indexDescription = "manual") {
        autoRefresh(false)
        field("id", FieldTypes.domainId) { isIdField() }
    }


}


/**
 * Fixtures for the EsDoc auto-refresh CrudListeners: every entity has full create/update/delete APIs so that
 * its CrudListener interface declares every callback.
 */
class EsRefreshListenerTestSpec(autoRefresh: Boolean = true) : AbstractSpec(AppKey("Test")) {


    val region = entity("com.example", "Region", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        crud {
            create { api {} }
            update { api {} }
            delete { api {} }
        }
    }


    val customer = entity("com.example", "Customer", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        field("notShown", FieldTypes.string) {
            fieldDisplayName("Not shown")
            editableByUser()
        }
        foreignKey("region", region, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Region")
        crud {
            create { api {} }
            update { api {} }
            delete { api {} }
        }
    }


    val order = entity("com.example", "Order", deletable = Deletable.TRUE) {
        field("orderNo", FieldTypes.string) {
            fieldDisplayName("Order no")
            editableByUser()
        }
        field("notShown", FieldTypes.string) {
            fieldDisplayName("Not shown")
            editableByUser()
        }
        foreignKey("customer", customer, isEditableByUser = IsEditableByUser.TRUE, fieldDisplayName = "Customer")
        crud {
            create { api {} }
            update { api {} }
            delete { api {} }
        }
    }


    /** Two auto-refreshed versions; the root has no many-to-many, so it diffs its watched fields. */
    val orderDto = searchableDto(baseName = "OrderEs", entityDef = order) {
        elasticsearchEnabled(version = 1, autoRefresh = autoRefresh)
        elasticsearchEnabled(version = 2, autoRefresh = autoRefresh)
        field("orderNo")
        field("customerName", "customer.name")
        field("customerRegionName", "customer.region.name")
    }


    /** Only version 1 is auto-refreshed. */
    val orderMixedDto = searchableDto(baseName = "OrderMixedEs", entityDef = order) {
        elasticsearchEnabled(version = 1, autoRefresh = true)
        elasticsearchEnabled(version = 2, autoRefresh = false)
        field("orderNo")
    }


    /** Selects only the root's id: the root watches no field, so its update callback has nothing to diff. */
    val orderIdOnlyDto = searchableDto(baseName = "OrderIdOnlyEs", entityDef = order) {
        elasticsearchEnabled()
        field("id")
    }


    val left = entity("com.example", "Left", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        crud {
            create { api {} }
            update { api {} }
            delete { api {} }
        }
    }


    val right = entity("com.example", "Right", nameFieldForPkAndNameDto = "name", deletable = Deletable.TRUE) {
        field("name", FieldTypes.string) {
            fieldDisplayName("Name")
            editableByUser()
        }
        crud {
            create { api {} }
            update { api {} }
            delete { api {} }
        }
    }


    val leftRightCrud = manyToManyEntity(
        "com.example",
        "LeftRightCrud",
        deletable = Deletable.TRUE,
        leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
        rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
    ) {
        field("weight", FieldTypes.int) {
            fieldDisplayName("Weight")
            editableByUser()
        }
        crud {
            create { api {} }
            update { api {} }
            delete { api {} }
        }
    }


    /** Many-to-many through a join entity with a CrudListener: root, join entity J and other side O. */
    val leftDto = searchableDto(baseName = "LeftEs", entityDef = left) {
        elasticsearchEnabled()
        field("name")
        manyToManyField("rights", leftRightCrud)
    }


    /** Right-rooted: the join entity's foreign key to the root is `right`. */
    val rightDto = searchableDto(baseName = "RightEs", entityDef = right) {
        elasticsearchEnabled()
        field("name")
        manyToManyField("lefts", leftRightCrud)
    }


    val leftRightSimple = simpleManyToManyEntity(
        "com.example",
        "LeftRightSimple",
        leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
        rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
    )


    /** The join entity has no CrudListener, so only the root and the other side get listeners. */
    val leftSimpleDto = searchableDto(baseName = "LeftSimpleEs", entityDef = left) {
        elasticsearchEnabled()
        field("name")
        manyToManyField("rights", leftRightSimple)
    }


    val leftRightCoded = manyToManyEntity(
        "com.example",
        "LeftRightCoded",
        leftEntity = ReferencedEntity("left", "Left", left, IsEditableByUser.TRUE),
        rightEntity = ReferencedEntity("right", "Right", right, IsEditableByUser.TRUE)
    ) {
        field("code", FieldTypes.string) { primaryKey() }
        crud {}
    }


    /** The join entity has a CrudListener but a non-DomainId primary key: no reverse join, but its root foreign key is a DomainId. */
    val leftCodedDto = searchableDto(baseName = "LeftCodedEs", entityDef = left) {
        elasticsearchEnabled()
        manyToManyJoin(leftRightCoded)
        field("name")
    }


}


object SearchableDtoEsTestSupport {


    val spec = SearchableDtoEsTestSpec()


    val applicationModelDef: ApplicationModelDef = object : ApplicationSpec("com.example") {
        override val modelDefs: List<ModelDef> = listOf(spec.modelDef)
    }.applicationModelDef


    fun kotlinFilesIn(dir: File): List<File> {
        return dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }


    fun singleKotlinFileIn(dir: File): String {

        val files = kotlinFilesIn(dir)
        assertThat(files).withFailMessage("Expected exactly one rendered .kt file but found: $files").hasSize(1)
        return files.single().readText()

    }


}
