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
        assertThat(derived.map { it.uqcn.value }).containsExactlyInAnyOrder("ChildEsEsDoc_v0001", "ChildEsEsDoc_v0002")
        assertThat(applicationModelDef.esDocsDefs.map { it.uqcn.value }).contains("ManualEsDoc_v0001")
        assertThat(derived).doesNotContainAnyElementsOf(applicationModelDef.esDocsDefs.filter { it.uqcn.value == "ManualEsDoc_v0001" })

    }


    @Test
    fun `elastic service generator renders a repo and index service per derived esdoc version and keeps the typeahead output`(@TempDir dir: File) {

        ElasticServiceModuleGenerator(context(dir)).generateSource(applicationModelDef)

        val names = kotlinFilesIn(dir).map { it.name }

        assertThat(names).containsExactlyInAnyOrder(
            "ChildEsEsDocRepo_v0001.kt", "ChildEsIndexService_v0001.kt",
            "ChildEsEsRefreshChildCrudListener_v0001.kt", "ChildEsEsRefreshParentCrudListener_v0001.kt",
            "ChildEsEsDocRepo_v0002.kt", "ChildEsIndexService_v0002.kt",
            "ChildEsEsRefreshChildCrudListener_v0002.kt", "ChildEsEsRefreshParentCrudListener_v0002.kt",
            "ParentTypeaheadIndexService_v0001.kt", "ParentTypeaheadEsDocRepo_v0001.kt"
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
            "OrderEsEsRefreshOrderCrudListener_v0001.kt",
            "OrderEsEsRefreshCustomerCrudListener_v0001.kt",
            "OrderEsEsRefreshRegionCrudListener_v0001.kt",
            "OrderEsEsRefreshOrderCrudListener_v0002.kt",
            "OrderEsEsRefreshCustomerCrudListener_v0002.kt",
            "OrderEsEsRefreshRegionCrudListener_v0002.kt",
            "OrderMixedEsEsRefreshOrderCrudListener_v0001.kt",
            "OrderIdOnlyEsEsRefreshOrderCrudListener_v0001.kt",
            "LeftEsEsRefreshLeftCrudListener_v0001.kt",
            "LeftEsEsRefreshLeftRightCrudCrudListener_v0001.kt",
            "LeftEsEsRefreshRightCrudListener_v0001.kt",
            "LeftSimpleEsEsRefreshLeftCrudListener_v0001.kt",
            "LeftSimpleEsEsRefreshRightCrudListener_v0001.kt",
            "LeftCodedEsEsRefreshLeftCrudListener_v0001.kt",
            "LeftCodedEsEsRefreshLeftRightCodedCrudListener_v0001.kt",
            "RightEsEsRefreshRightCrudListener_v0001.kt",
            "RightEsEsRefreshLeftRightCrudCrudListener_v0001.kt",
            "RightEsEsRefreshLeftCrudListener_v0001.kt"
        )

        // the non auto-refresh version still gets its index service
        assertThat(kotlinFilesIn(dir).map { it.name }).contains("OrderMixedEsIndexService_v0002.kt")

    }


    @Test
    fun `elastic service generator renders no listeners for a dto whose versions are all autoRefresh false`(@TempDir dir: File) {

        val noAutoRefreshModelDef = object : ApplicationSpec("com.example") {
            override val modelDefs: List<ModelDef> = listOf(EsRefreshListenerTestSpec(autoRefresh = false).modelDef)
        }.applicationModelDef

        ElasticServiceModuleGenerator(context(dir)).generateSource(noAutoRefreshModelDef)

        assertThat(kotlinFilesIn(dir).map { it.name }.filter { it.startsWith("OrderEs") }).containsExactlyInAnyOrder(
            "OrderEsEsDocRepo_v0001.kt", "OrderEsIndexService_v0001.kt",
            "OrderEsEsDocRepo_v0002.kt", "OrderEsIndexService_v0002.kt"
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
