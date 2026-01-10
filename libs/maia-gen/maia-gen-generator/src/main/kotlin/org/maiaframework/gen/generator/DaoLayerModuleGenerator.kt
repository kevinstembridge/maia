package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.*
import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.flags.WithGeneratedDto


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = DaoLayerModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class DaoLayerModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        `render create-table scripts`()
        `render DaoIndexCreators`()
        `render EntityRowMappers`()
        `render EntityPkRowMappers`()
        `render ForEditDtoRowMappers`()
        `process SearchableDtoDefs`()
        `render DAOs`()
        `render SearchableDtoSearchConverters`()
        `render DtoDocumentMappers`()
        `render TableDtoDocumentMappers`()

    }


    private fun `render create-table scripts`() {

        val sqlScriptsDir = this.maiaGenerationContext.sqlCreateScriptsDir

        val jdbcRootEntityHierarchies = this.modelDef.rootEntityHierarchies.filter { it.entityDef.databaseType == DatabaseType.JDBC }
        val renderedFileName = "${this.maiaGenerationContext.createTablesSqlScriptPrefix}_${this.modelDef.appKey}.sql"

        if (jdbcRootEntityHierarchies.isNotEmpty()) {
            CreateTableSqlRenderer(jdbcRootEntityHierarchies, renderedFileName).renderToDir(sqlScriptsDir)
//            RenameColumnsSqlRenderer(jdbcRootEntityHierarchies, renderedFileName).renderToDir(sqlScriptsDir)
        }

    }


    private fun `process SearchableDtoDefs`() {

        this.modelDef.allSearchableDtoDefs.forEach {

            if (it.dtoRootEntityDef.databaseType == DatabaseType.JDBC) {

                SearchableDtoJdbcDaoRenderer(it, this.modelDef).renderToDir(this.kotlinOutputDir)
                RowMapperRenderer(it.rowMapperDef).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render DAOs`() {

        this.modelDef.entityHierarchies.forEach { entityHierarchy ->

            when (entityHierarchy.entityDef.databaseType) {
                DatabaseType.JDBC -> JdbcDaoRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)
                DatabaseType.MONGO -> MongoDaoRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)
            }

        }

    }


    private fun `render DaoIndexCreators`() {

        this.modelDef.entityHierarchies.map { it.entityDef }
            .filter { it.databaseType == DatabaseType.MONGO }
            .filter { it.databaseIndexDefs.isNotEmpty() }
            .forEach { entity ->
                MongoDaoIndexCreatorRenderer(entity).renderToDir(this.kotlinOutputDir)
            }

    }


    private fun `render EntityRowMappers`() {

        this.modelDef.entityHierarchies.forEach { renderEntityRowMapper(it) }

    }


    private fun renderEntityRowMapper(entityHierarchy: EntityHierarchy) {

        if (entityHierarchy.entityDef.databaseType == DatabaseType.JDBC) {
            EntityRowMapperRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render EntityPkRowMappers`() {

        this.modelDef.entityHierarchies.forEach { renderEntityPkRowMapper(it) }

    }


    private fun renderEntityPkRowMapper(entityHierarchy: EntityHierarchy) {

        if (entityHierarchy.entityDef.databaseType == DatabaseType.JDBC) {

            entityHierarchy.entityDef.primaryKeyRowMapperDef?.let { rowMapperDef ->
                RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
            }
        }

    }


    private fun `render ForEditDtoRowMappers`() {

        this.modelDef.fetchForEditDtoDefs.filter { it.databaseType == DatabaseType.JDBC }.forEach { dtoDef ->

            val rowMapperDef = RowMapperDef(
                dtoDef.uqcn,
                dtoDef.rowMapperFieldDefs,
                dtoDef.rowMapperClassDef,
                isForEditDto = true
            )

            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render SearchableDtoSearchConverters`() {

        this.modelDef.allSearchableDtoDefs.filter { it.withGeneratedDto == WithGeneratedDto.TRUE }.forEach { renderSearchableDtoSearchConverter(it) }

    }


    private fun renderSearchableDtoSearchConverter(searchableDtoDef: SearchableDtoDef) {

        if (searchableDtoDef.dtoRootEntityDef.databaseType == DatabaseType.MONGO) {
//            SearchableDtoSearchConverterRenderer(searchableDtoDef).renderToDir(this.outputDir)
            SearchableDtoSearchConverterRenderer_exp(searchableDtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render DtoDocumentMappers`() {

        this.modelDef.allSearchableDtoDefs.filter { it.withGeneratedDto == WithGeneratedDto.TRUE }.forEach { this.renderDocumentMapper(it) }

    }


    private fun renderDocumentMapper(searchableDtoDef: SearchableDtoDef) {

        if (searchableDtoDef.dtoRootEntityDef.databaseType == DatabaseType.MONGO) {
            DtoDocumentMapperRenderer(searchableDtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render TableDtoDocumentMappers`() {

        this.modelDef.dtoHtmlTableDefs
                .filter { it.dtoHtmlTableSourceDef.databaseType == DatabaseType.MONGO }
                .forEach { renderTableDtoDocumentMapper(it) }

    }


    private fun renderTableDtoDocumentMapper(dtoHtmlTableDef: DtoHtmlTableDef) {

        val searchableDtoDef = dtoHtmlTableDef.dtoHtmlTableSourceDef.searchableDtoDef!!

        val fieldDefs = dtoHtmlTableDef.dtoHtmlTableColumnFields.map {
            searchableDtoDef.findDocumentFieldByPath(it.dtoFieldName)
        }

        val documentMapperDef = DocumentMapperDef(
                dtoHtmlTableDef.searchDtoDef.fqcn,
                searchableDtoDef.tableName,
                fieldDefs
        )

        TableDtoDocumentMapperRenderer(documentMapperDef).renderToDir(this.kotlinOutputDir)

    }


}
