package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EntityDetailDtoRepoRenderer
import org.maiaframework.gen.renderers.EntityRepoRenderer
import org.maiaframework.gen.renderers.ResponseDtoRepoRenderer
import org.maiaframework.gen.renderers.SearchableDtoMongoRepoRenderer
import org.maiaframework.gen.renderers.SearchableTableDtoJdbcRepoRenderer
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.SearchableDtoDef


class RepoModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
) : AbstractModuleGenerator(
    modelDef,
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
