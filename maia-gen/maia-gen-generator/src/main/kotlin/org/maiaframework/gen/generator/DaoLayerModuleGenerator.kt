package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.CreateTableSqlRenderer
import org.maiaframework.gen.renderers.EntityHistoryBlotterRowDtoDaoRenderer
import org.maiaframework.gen.renderers.EntityHistoryBlotterRowMapperRenderer
import org.maiaframework.gen.renderers.EntityRowMapperRenderer
import org.maiaframework.gen.renderers.JdbcDaoRenderer
import org.maiaframework.gen.renderers.RowMapperRenderer
import org.maiaframework.gen.renderers.SearchableDtoJdbcDaoRenderer
import org.maiaframework.gen.spec.definition.EntityHierarchy


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)
        val moduleGenerator = DaoLayerModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

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
        `render EntityRowMappers`()
        `render EntityPkRowMappers`()
        `render ForEditDtoRowMappers`()
        `render RowMapperDefs`()
        `process SearchableDtoDefs`()
        `render DAOs`()
        `render EntityHistoryBlotterRowMappers`()
        `render EntityHistoryBlotterRowDtoDaos`()

    }


    private fun `render create-table scripts`() {

        val sqlScriptsDir = this.maiaGenerationContext.sqlCreateScriptsDir

        val jdbcRootEntityHierarchies = this.applicationModelDef.rootEntityHierarchies
        val renderedFileName = "${this.maiaGenerationContext.createTablesSqlScriptPrefix}.sql"

        if (jdbcRootEntityHierarchies.isNotEmpty()) {
            CreateTableSqlRenderer(jdbcRootEntityHierarchies, renderedFileName).renderToDir(sqlScriptsDir)
        }

    }


    private fun `process SearchableDtoDefs`() {

        this.applicationModelDef.allSearchableDtoDefs.forEach {

            SearchableDtoJdbcDaoRenderer(it, this.applicationModelDef).renderToDir(this.kotlinOutputDir)
            RowMapperRenderer(it.rowMapperDef).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render DAOs`() {

        this.applicationModelDef.entityHierarchies.forEach { entityHierarchy ->

            JdbcDaoRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render EntityRowMappers`() {

        this.applicationModelDef.entityHierarchies.forEach { renderEntityRowMapper(it) }

    }


    private fun renderEntityRowMapper(entityHierarchy: EntityHierarchy) {

        EntityRowMapperRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EntityPkRowMappers`() {

        this.applicationModelDef.entityHierarchies.forEach { renderEntityPkRowMapper(it) }

    }


    private fun renderEntityPkRowMapper(entityHierarchy: EntityHierarchy) {

        entityHierarchy.entityDef.primaryKeyRowMapperDef?.let { rowMapperDef ->
            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render ForEditDtoRowMappers`() {

        this.applicationModelDef.fetchForEditDtoDefs.map { it.rowMapperDef }.forEach { rowMapperDef ->
            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render RowMapperDefs`() {

        this.applicationModelDef.rowMapperDefs.forEach { rowMapperDef ->
            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render EntityHistoryBlotterRowMappers`() {

        this.applicationModelDef.entityHistoryBlotterDefs.forEach { def ->
            EntityHistoryBlotterRowMapperRenderer(def).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render EntityHistoryBlotterRowDtoDaos`() {

        this.applicationModelDef.entityHistoryBlotterDefs.forEach { def ->
            EntityHistoryBlotterRowDtoDaoRenderer(def).renderToDir(this.kotlinOutputDir)
        }

    }


}
