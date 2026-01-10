package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.*
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.SearchableDtoDef


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = RepoLayerModuleGenerator(moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class RepoLayerModuleGenerator(
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        `render Entity repos`()
        `render ResponseDto repos`()
        `render SearchableDto repos`()
        `render EntityDetailDto repos`()

    }


    private fun `render Entity repos`() {

        this.modelDef.entityHierarchies.forEach {
            EntityRepoRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render ResponseDto repos`() {

        this.modelDef.responseDtoDefs.forEach {
            ResponseDtoRepoRenderer(it).renderToDir(kotlinOutputDir)
        }

    }


    private fun `render SearchableDto repos`() {

        modelDef.allSearchableDtoDefs.forEach {

            processSearchableDtoDef(it)

        }

    }


    private fun processSearchableDtoDef(searchableDtoDef: SearchableDtoDef) {

        when (searchableDtoDef.dtoRootEntityDef.databaseType) {
            DatabaseType.JDBC -> SearchableTableDtoJdbcRepoRenderer(searchableDtoDef).renderToDir(kotlinOutputDir)
            DatabaseType.MONGO -> SearchableDtoMongoRepoRenderer(searchableDtoDef).renderToDir(kotlinOutputDir)
        }

    }


    private fun `render EntityDetailDto repos`() {

        modelDef.entityDetailDtoDefs.forEach {

            EntityDetailDtoRepoRenderer(it).renderToDir(kotlinOutputDir)

        }

    }


}
