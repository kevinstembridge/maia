package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.Description
import org.maiaframework.gen.spec.definition.EntityBaseName
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.FieldDisplayName
import org.maiaframework.gen.spec.definition.ForeignKeyFieldDef
import org.maiaframework.gen.spec.definition.FormPlaceholderText
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.flags.IsCreatableByUser
import org.maiaframework.gen.spec.definition.flags.IsDeltaField
import org.maiaframework.gen.spec.definition.flags.IsDeltaKey
import org.maiaframework.gen.spec.definition.flags.IsDerived
import org.maiaframework.gen.spec.definition.flags.IsEditableByUser
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Nullability
import org.maiaframework.gen.spec.definition.lang.PackageName


@MaiaDslMarker
class ForeignKeyFieldDefBuilder(
    private val foreignKeyFieldName: ClassFieldName,
    private val foreignKeyEntityDef: EntityDef
) {


    private var displayName: FieldDisplayName? = null


    private var formPlaceholderText: FormPlaceholderText? = null


    private var typeaheadDef: TypeaheadDef? = null


    private var searchableDtoDef: SearchableDtoDef? = null


    private var searchTermFieldName: String? = null


    private var modifiableBySystem: Boolean = false


    private var isEditableByUser: IsEditableByUser = IsEditableByUser.FALSE


    private var tableColumnName: TableColumnName? = null


    private var isCreatableByUser: IsCreatableByUser = IsCreatableByUser.TRUE


    private var nullability: Nullability = Nullability.NOT_NULLABLE


    fun buildEntityFieldDef(
        entityBaseName: EntityBaseName,
        packageName: PackageName
    ): EntityFieldDef {

        val foreignKeyFieldDef = ForeignKeyFieldDef(
            foreignKeyFieldName,
            foreignKeyEntityDef,
            typeaheadDef,
            searchableDtoDef,
            searchTermFieldName
        )

        val classFieldDef = ClassFieldDef(
            foreignKeyFieldName.withSuffix("Id"),
            description = Description("Foreign key to the ${foreignKeyEntityDef.entityBaseName} entity. (table = ${foreignKeyEntityDef.tableName})"),
            fieldType = FieldTypes.foreignKey(foreignKeyFieldDef),
            isModifiableBySystem = this.modifiableBySystem,
            isEditableByUser = this.isEditableByUser,
            nullability = this.nullability,
            isPrivateProperty = false,
            isConstructorOnly = false,
            annotationDefs = sortedSetOf(),
            providedValidationConstraints = sortedSetOf(),
            displayName = displayName,
            formPlaceholderText = formPlaceholderText,
            typeaheadDef = this.typeaheadDef
        )

        return EntityFieldDef(
            entityBaseName,
            packageName,
            classFieldDef,
            isDeltaField = IsDeltaField.FALSE,
            isDeltaKey = IsDeltaKey.FALSE,
            isDerived = IsDerived.FALSE,
            providedTableColumnName = tableColumnName,
            isCreatableByUser = this.isCreatableByUser
        )

    }


    fun modifiableBySystem() {

        this.modifiableBySystem = true

    }


    fun editableByUser() {

        this.isEditableByUser = IsEditableByUser.TRUE

    }


    fun nullable() {

        this.nullability = Nullability.NULLABLE

    }


    fun fieldDisplayName(fieldDisplayName: String) {

        this.displayName = FieldDisplayName(fieldDisplayName)

    }


    fun formPlaceholderText(text: String) {

        this.formPlaceholderText = FormPlaceholderText(text)

    }


    fun typeaheadField(typeaheadDef: TypeaheadDef) {

        this.typeaheadDef = typeaheadDef

    }


    fun tableColumnName(name: String) {

        this.tableColumnName = TableColumnName(name)

    }


    fun notCreatableByUser() {

        this.isCreatableByUser = IsCreatableByUser.FALSE

    }


}
