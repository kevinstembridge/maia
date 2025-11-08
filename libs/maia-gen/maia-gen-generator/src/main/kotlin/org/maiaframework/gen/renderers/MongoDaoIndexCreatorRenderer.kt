package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.DatabaseIndexDef
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.IndexDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import java.util.concurrent.TimeUnit

class MongoDaoIndexCreatorRenderer(private val entityDef: EntityDef) : AbstractKotlinRenderer(entityDef.daoIndexCreatorClassDef) {


    init {

        addConstructorArg(aClassField("mongoClientFacade", Fqcns.MONGO_CLIENT_FACADE).constructorOnly().build())

    }


    override fun renderPreClassFields() {

        addImportRaw("org.maiaframework.common.logging.getLogger")
        addImportFor(Fqcns.MAHANA_MONGO_COLLECTION_FACADE)

        blankLine()
        appendLine("    private val logger = getLogger<${classDef.uqcn}>()")
        blankLine()
        blankLine()
        appendLine("    private val mongoCollectionFacade: MongoCollectionFacade =")
        appendLine("            MongoCollectionFacade(${this.entityDef.metaClassDef.uqcn}.COLLECTION_NAME, mongoClientFacade)")

    }


    override fun renderFunctions() {

        renderFunctionCreateIndexes()
        renderCreateIndexFunctions()
        renderCreateIndex()

    }


    private fun renderFunctionCreateIndexes() {

        blankLine()
        blankLine()
        appendLine("    fun createIndexes() {")
        blankLine()

        this.entityDef.databaseIndexDefs.forEach {
            appendLine("        createIndex_${it.indexDef.indexName}()")
        }

        blankLine()
        appendLine("    }")

    }


    private fun renderCreateIndexFunctions() {

        this.entityDef.databaseIndexDefs.forEach { this.renderCreateIndex(it) }

    }


    private fun renderCreateIndex(databaseIndexDef: DatabaseIndexDef) {

        if (databaseIndexDef.indexDef.fieldCount > 1) {
            renderCreateCompoundIndex(databaseIndexDef.indexDef)
        } else {
            renderCreateIndexForSingleField(databaseIndexDef.indexDef)
        }

    }


    private fun renderCreateIndexForSingleField(indexDef: IndexDef) {

        addImportFor(Fqcns.MONGO_INDEX_OPTIONS)
        addImportFor(Fqcns.MONGO_INDEXES)

        blankLine()
        blankLine()

        appendLine("    private fun createIndex_${indexDef.indexName}() {")
        blankLine()
        appendLine("        val indexOptions = IndexOptions()")
        appendLine("        indexOptions.name(\"${indexDef.indexName}\")")

        if (indexDef.isUnique) {
            appendLine("        indexOptions.unique(true)")
        }

        if (indexDef.isSparse) {
            appendLine("        indexOptions.sparse(true)")
        }

        indexDef.expireAfterSeconds?.let { ttl ->
            addImportFor(TimeUnit::class.java)
            appendLine("        indexOptions.expireAfter(${ttl}L, TimeUnit.SECONDS)")
        }

        val indexFieldDef = indexDef.indexFieldDefs.firstOrNull()
                ?: throw IllegalStateException("Expected index named " + indexDef.indexName + " to have one indexed field but found none")

        appendLine("        createIndex(Indexes.${if (indexFieldDef.isAscending) "ascending" else "descending"}(\"${indexFieldDef.databaseColumnName}\"), indexOptions)")
        blankLine()
        appendLine("    }")

    }


    private fun renderCreateCompoundIndex(indexDef: IndexDef) {

        addImportFor(Fqcns.MONGO_INDEX_OPTIONS)
        addImportFor(Fqcns.MONGO_INDEXES)

        blankLine()
        blankLine()

        appendLine("    private fun createIndex_${indexDef.indexName}() {")
        blankLine()
        appendLine("        val indexBson: Bson = Indexes.compoundIndex(")

        appendLines(
                indexDef.indexFieldDefs.map { fd -> { append("                Indexes.${if (fd.isAscending) "ascending" else "descending"}(\"${fd.databaseColumnName}\")") } }
        ) { appendLine(",") }

        appendLine(")")
        blankLine()
        appendLine("        val indexOptions = IndexOptions()")
        appendLine("        indexOptions.name(\"${indexDef.indexName}\")")

        if (indexDef.isUnique) {
            appendLine("        indexOptions.unique(true)")
        }

        if (indexDef.isSparse) {
            appendLine("        indexOptions.sparse(true)")
        }

        indexDef.expireAfterSeconds?.let { ttl ->
            addImportFor(TimeUnit::class.java)
            appendLine("        indexOptions.expireAfter(ttl, TimeUnit.SECONDS)")
        }

        blankLine()
        appendLine("        createIndex(indexBson, indexOptions)")
        blankLine()
        appendLine("    }")

    }


    private fun renderCreateIndex() {

        addImportFor(Fqcns.BSON)

        blankLine()
        blankLine()
        appendLine("    private fun createIndex(keys: Bson, indexOptions: IndexOptions) {")
        blankLine()
        appendLine("        this.mongoCollectionFacade.createIndex(keys, indexOptions)")
        blankLine()
        appendLine("    }")

    }


}
