package org.maiaframework.gen.generator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.renderers.EsRefreshListenerTestSpec
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.applicationModelDef
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.kotlinFilesIn
import org.maiaframework.gen.renderers.SearchableDtoEsTestSupport.spec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.ModelDef
import java.io.File


class SearchableDtoEsGeneratorWiringTest {


    private fun context(dir: File) = MaiaGenerationContext(
        srcMainKotlinOutputDir = dir,
        srcTestKotlinOutputDir = dir,
        srcMainResourcesDir = dir,
        srcTestResourcesDir = dir,
        typescriptOutputDir = dir,
        sqlCreateScriptsDir = dir,
        createTablesSqlScriptPrefix = "V",
    )


    @Test
    fun `derivedEsDocDefs is exactly the searchable dto esDocDefs and none of the manual ones`() {

        val derived = derivedEsDocDefs(applicationModelDef)

        assertThat(derived).containsExactlyElementsOf(spec.esDto.esDocDefs)
        assertThat(derived.map { it.uqcn.value }).containsExactlyInAnyOrder("ChildEsV1EsDoc", "ChildEsV2EsDoc")
        assertThat(applicationModelDef.esDocsDefs.map { it.uqcn.value }).contains("ManualV1EsDoc")
        assertThat(derived).doesNotContainAnyElementsOf(applicationModelDef.esDocsDefs.filter { it.uqcn.value == "ManualV1EsDoc" })

    }


    @Test
    fun `elastic service generator renders a repo and index service per derived esdoc version and keeps the typeahead output`(@TempDir dir: File) {

        ElasticServiceModuleGenerator(context(dir)).generateSource(applicationModelDef)

        val names = kotlinFilesIn(dir).map { it.name }

        assertThat(names).containsExactlyInAnyOrder(
            "ChildEsV1EsDocRepo.kt", "ChildEsV1IndexService.kt",
            "ChildEsV1EsRefreshChildCrudListener.kt", "ChildEsV1EsRefreshParentCrudListener.kt",
            "ChildEsV2EsDocRepo.kt", "ChildEsV2IndexService.kt",
            "ChildEsV2EsRefreshChildCrudListener.kt", "ChildEsV2EsRefreshParentCrudListener.kt",
            "ParentTypeaheadV1IndexService.kt", "ParentTypeaheadV1EsDocRepo.kt"
        )

    }


    @Test
    fun `elastic service generator renders auto-refresh listeners only for auto-refresh versions`(@TempDir dir: File) {

        val listenerModelDef = object : ApplicationSpec("com.example") {
            override val modelDefs: List<ModelDef> = listOf(EsRefreshListenerTestSpec().modelDef)
        }.applicationModelDef

        ElasticServiceModuleGenerator(context(dir)).generateSource(listenerModelDef)

        val listenerNames = kotlinFilesIn(dir).map { it.name }.filter { "EsRefresh" in it }

        assertThat(listenerNames).containsExactlyInAnyOrder(
            "OrderEsV1EsRefreshOrderCrudListener.kt",
            "OrderEsV1EsRefreshCustomerCrudListener.kt",
            "OrderEsV1EsRefreshRegionCrudListener.kt",
            "OrderEsV2EsRefreshOrderCrudListener.kt",
            "OrderEsV2EsRefreshCustomerCrudListener.kt",
            "OrderEsV2EsRefreshRegionCrudListener.kt",
            "OrderMixedEsV1EsRefreshOrderCrudListener.kt",
            "OrderIdOnlyEsV1EsRefreshOrderCrudListener.kt",
            "LeftEsV1EsRefreshLeftCrudListener.kt",
            "LeftEsV1EsRefreshLeftRightCrudCrudListener.kt",
            "LeftEsV1EsRefreshRightCrudListener.kt",
            "LeftSimpleEsV1EsRefreshLeftCrudListener.kt",
            "LeftSimpleEsV1EsRefreshRightCrudListener.kt",
            "LeftCodedEsV1EsRefreshLeftCrudListener.kt",
            "LeftCodedEsV1EsRefreshLeftRightCodedCrudListener.kt",
            "RightEsV1EsRefreshRightCrudListener.kt",
            "RightEsV1EsRefreshLeftRightCrudCrudListener.kt",
            "RightEsV1EsRefreshLeftCrudListener.kt"
        )

        // the non auto-refresh version still gets its index service
        assertThat(kotlinFilesIn(dir).map { it.name }).contains("OrderMixedEsV2IndexService.kt")

    }


    @Test
    fun `elastic service generator renders no listeners for a dto whose versions are all autoRefresh false`(@TempDir dir: File) {

        val noAutoRefreshModelDef = object : ApplicationSpec("com.example") {
            override val modelDefs: List<ModelDef> = listOf(EsRefreshListenerTestSpec(autoRefresh = false).modelDef)
        }.applicationModelDef

        ElasticServiceModuleGenerator(context(dir)).generateSource(noAutoRefreshModelDef)

        assertThat(kotlinFilesIn(dir).map { it.name }.filter { it.startsWith("OrderEs") }).containsExactlyInAnyOrder(
            "OrderEsV1EsDocRepo.kt", "OrderEsV1IndexService.kt",
            "OrderEsV2EsDocRepo.kt", "OrderEsV2IndexService.kt"
        )

    }


    @Test
    fun `elastic search generator renders controls for derived esdocs only`(@TempDir dir: File) {

        ElasticSearchModuleGenerator(context(dir)).generateSource(applicationModelDef)

        val controls = kotlinFilesIn(dir).map { it.name }.filter { "EsIndexControl" in it }

        // typeahead controls come from the existing typeahead path, derived ones from the new path, none for manual esDoc{}
        assertThat(controls).containsExactlyInAnyOrder(
            "ChildEsEsIndexControl_v0001.kt",
            "ChildEsEsIndexControl_v0002.kt",
            "ParentTypeaheadEsIndexControl_v0001.kt"
        )

    }


}
