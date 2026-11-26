package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class EntityRefDef(
    val packageName: PackageName,
    val dtoBaseName: DtoBaseName,
    val pkEntityFieldDef: EntityFieldDef,
    val displayNameEntityFieldDef: EntityFieldDef,
    val entityRepoClassDef: ClassDef,
    val shortcodeEntityFieldDef: EntityFieldDef? = null
) {


    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName.withSuffix("Ref"),
        DtoSuffix("Dto"),
        listOfNotNull(
            aClassField(ID_FIELD, pkEntityFieldDef.fieldType).build(),
            aClassField(DISPLAY_NAME_FIELD, displayNameEntityFieldDef.fieldType).build(),
            shortcodeEntityFieldDef?.let { aClassField(SHORTCODE_FIELD, it.fieldType).build() },
        )
    ).build()


    val refDtoFqcn = dtoDef.fqcn


    val rowMapperDef = RowMapperDef(
        dtoDef.fqcn,
        listOfNotNull(
            EntityFieldRowMapperFieldDef(ClassFieldName(ID_FIELD), pkEntityFieldDef, pkEntityFieldDef.tableColumnName.value),
            EntityFieldRowMapperFieldDef(ClassFieldName(DISPLAY_NAME_FIELD), displayNameEntityFieldDef, displayNameEntityFieldDef.tableColumnName.value),
            shortcodeEntityFieldDef?.let { EntityFieldRowMapperFieldDef(ClassFieldName(SHORTCODE_FIELD), it, it.tableColumnName.value) }
        ),
        dtoDef.rowMapperClassDef,
        isForEditDto = true,
        compositeIdFields = emptyList()
    )


    val dtoUqcn = dtoDef.uqcn


    companion object {

        /** The names of the RefDto's fields, which are also the names of the sub-fields of a ref held in an Elasticsearch document. */
        const val ID_FIELD = "id"
        const val DISPLAY_NAME_FIELD = "displayName"
        const val SHORTCODE_FIELD = "shortcode"

        /** The keyword sub-field of the text-mapped display name, which the Blotter filters and sorts on. */
        const val RAW_SUB_FIELD = "raw"

        /** The SQL result-set column holding the ref's id for a Searchable DTO field called [fieldName]. */
        fun idResultSetColumn(fieldName: Any) = "${fieldName}Id"

        /** The SQL result-set column holding the ref's display name for a Searchable DTO field called [fieldName]. */
        fun displayNameResultSetColumn(fieldName: Any) = "${fieldName}Name"

        /** The SQL result-set column holding the ref's shortcode for a Searchable DTO field called [fieldName]. */
        fun shortcodeResultSetColumn(fieldName: Any) = "${fieldName}Shortcode"

    }


    /**
     * Kotlin source for constructing the DTO from an entity held in a variable named [entityVar].
     */
    fun constructionFrom(entityVar: String, indent: String): String {

        val args = listOfNotNull(
            ID_FIELD to pkEntityFieldDef,
            DISPLAY_NAME_FIELD to displayNameEntityFieldDef,
            shortcodeEntityFieldDef?.let { SHORTCODE_FIELD to it }
        ).joinToString(",\n") { (dtoFieldName, entityFieldDef) -> "$indent    $dtoFieldName = $entityVar.${entityFieldDef.classFieldName}" }

        return "$dtoUqcn(\n$args\n$indent)"

    }


    val refDtoImportStatement = "import { ${refDtoFqcn.uqcn} } from '@${GeneratedTypescriptDir.forPackage(refDtoFqcn.packageName)}/${refDtoFqcn.uqcn}';"


    val refDtoTypescriptImport = TypescriptImport(refDtoFqcn.uqcn.value, "@${GeneratedTypescriptDir.forPackage(refDtoFqcn.packageName)}/${refDtoFqcn.uqcn}")


}
