package org.maiaframework.gen.spec.definition.builders


import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.SearchableDtoLookupDef
import org.maiaframework.gen.spec.definition.SearchableDtoLookupFieldDef
import org.maiaframework.gen.spec.definition.lang.*


@MaiaDslMarker
class SearchableDtoLookupDefBuilder(
    private val foreignKeyEntityDef: EntityDef,
    private val localFieldClassFieldName: ClassFieldName,
    private val foreignFieldDef: EntityFieldDef,
    private val parentBuilder: SearchableDtoDefBuilder,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {

    private val fieldDefBuilders = mutableListOf<SearchableDtoLookupFieldDefBuilder>()


    fun build(): SearchableDtoLookupDef {

        val fieldDefs = buildFieldDefs()

        return SearchableDtoLookupDef(
            this.foreignKeyEntityDef,
            this.localFieldClassFieldName,
            this.foreignFieldDef,
            fieldDefs
        )

    }

    private fun buildFieldDefs(): List<SearchableDtoLookupFieldDef> {

        return this.fieldDefBuilders.map { it.build() }
    }


    fun lookupField(foreignFieldName: String): SearchableDtoLookupFieldDefBuilder {

        val foreignFieldDef = this.foreignKeyEntityDef.findFieldByName(foreignFieldName)

        val builder = SearchableDtoLookupFieldDefBuilder(
            foreignFieldDef,
            this,
            this.defaultFieldTypeFieldReaderProvider,
            this.defaultFieldTypeFieldWriterProvider
        )

        this.fieldDefBuilders.add(builder)
        return builder

    }


    fun and(): SearchableDtoDefBuilder {

        return this.parentBuilder

    }


}
