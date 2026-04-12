package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.CreateTableSqlRenderer
import org.maiaframework.gen.renderers.EntityRowMapperRenderer
import org.maiaframework.gen.renderers.JdbcDaoRenderer
import org.maiaframework.gen.renderers.RowMapperRenderer
import org.maiaframework.gen.renderers.SearchableDtoJdbcDaoRenderer
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.RowMapperDef


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
        `render EntityRowMappers`()
        `render EntityPkRowMappers`()
        `render ForEditDtoRowMappers`()
        `render RowMapperDefs`()
        `process SearchableDtoDefs`()
        `render DAOs`()

    }


    private fun `render create-table scripts`() {

        val sqlScriptsDir = this.maiaGenerationContext.sqlCreateScriptsDir

        val jdbcRootEntityHierarchies = this.modelDef.rootEntityHierarchies
        val renderedFileName = "${this.maiaGenerationContext.createTablesSqlScriptPrefix}_${this.modelDef.appKey}.sql"

        if (jdbcRootEntityHierarchies.isNotEmpty()) {
            CreateTableSqlRenderer(jdbcRootEntityHierarchies, renderedFileName).renderToDir(sqlScriptsDir)
        }

    }


    private fun `process SearchableDtoDefs`() {

        this.modelDef.allSearchableDtoDefs.forEach {

            SearchableDtoJdbcDaoRenderer(it, this.modelDef).renderToDir(this.kotlinOutputDir)
            RowMapperRenderer(it.rowMapperDef).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render DAOs`() {

        this.modelDef.entityHierarchies.forEach { entityHierarchy ->

            JdbcDaoRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render EntityRowMappers`() {

        this.modelDef.entityHierarchies.forEach { renderEntityRowMapper(it) }

    }


    private fun renderEntityRowMapper(entityHierarchy: EntityHierarchy) {

        EntityRowMapperRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EntityPkRowMappers`() {

        this.modelDef.entityHierarchies.forEach { renderEntityPkRowMapper(it) }

    }


    private fun renderEntityPkRowMapper(entityHierarchy: EntityHierarchy) {

        entityHierarchy.entityDef.primaryKeyRowMapperDef?.let { rowMapperDef ->
            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render ForEditDtoRowMappers`() {

        this.modelDef.fetchForEditDtoDefs.map { it.rowMapperDef }.forEach { rowMapperDef ->
            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render RowMapperDefs`() {

        this.modelDef.rowMapperDefs.forEach { rowMapperDef ->
            RowMapperRenderer(rowMapperDef).renderToDir(this.kotlinOutputDir)
        }

    }


}
