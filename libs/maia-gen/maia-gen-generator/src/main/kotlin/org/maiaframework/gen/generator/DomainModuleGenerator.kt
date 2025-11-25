package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.AuthorityEnumRenderer
import org.maiaframework.gen.renderers.CrudListenerRenderer
import org.maiaframework.gen.renderers.DataClassRenderer
import org.maiaframework.gen.renderers.DtoRenderer
import org.maiaframework.gen.renderers.EntityFieldConverterRenderer
import org.maiaframework.gen.renderers.EntityFilterRenderer
import org.maiaframework.gen.renderers.EntityFiltersRenderer
import org.maiaframework.gen.renderers.EntityMetaRenderer
import org.maiaframework.gen.renderers.EntityPkRenderer
import org.maiaframework.gen.renderers.EntityRenderer
import org.maiaframework.gen.renderers.EntityUpdaterRenderer
import org.maiaframework.gen.renderers.EnumRenderer
import org.maiaframework.gen.renderers.FormModelRenderer
import org.maiaframework.gen.renderers.HazelcastEntityConfigRenderer
import org.maiaframework.gen.renderers.HazelcastSerializerRenderer
import org.maiaframework.gen.renderers.RequestDtoRenderer
import org.maiaframework.gen.renderers.ResponseDtoCsvHelperRenderer
import org.maiaframework.gen.renderers.SearchRequestFieldConverterRenderer
import org.maiaframework.gen.renderers.SearchRequestFieldNameConverterRenderer
import org.maiaframework.gen.renderers.SearchRequestSearchParserRenderer
import org.maiaframework.gen.renderers.SearchableDtoMetaRenderer
import org.maiaframework.gen.renderers.SimpleTypeRenderer
import org.maiaframework.gen.renderers.StagingEntityExtensionsRenderer
import org.maiaframework.gen.renderers.StringTypeRenderer
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.HazelcastDtoDef
import org.maiaframework.gen.spec.definition.InlineEditDtoDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.RequestDtoFieldDef
import org.maiaframework.gen.spec.definition.ResponseDtoDef
import org.maiaframework.gen.spec.definition.SearchableDtoDef
import org.maiaframework.gen.spec.definition.SimpleResponseDtoDef
import org.maiaframework.gen.spec.definition.flags.WithGeneratedEndpoint
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = DomainModuleGenerator(it, moduleGeneratorFixture.modelGeneratorContext)
            modelGenerator.generateSource()

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class DomainModuleGenerator(
    modelDef: ModelDef,
    modelGeneratorContext: ModelGeneratorContext
): AbstractModuleGenerator(
    modelDef,
    modelGeneratorContext
) {


    override fun onGenerateSource() {

        `process CrudApiDefs`()
        `process entities`()
        `process HazelcastDtoDefs`()
        `render async validation DTOs`()
        `render Authority enum`()
        `render BooleanTypes`()
        `render data classes`()
        `render EntityDetailDtos`()
        `render enums`()
        `render EsDocs`()
        `render FetchForEditDtos`()
        `render form models`()
        `render HazelcastEntityConfig`()
        `render IdAndNameDtos`()
        `render IntTypes`()
        `render LongTypes`()
        `render RequestDtos`()
        `render ResponseDtos`()
        `render SearchableDtos`()
        `render SimpleResponseDtos`()
        `render StringTypes`()
        `render TableDtos`()
        `render CrudListeners`()

    }


    private fun `render Authority enum`() {

        this.modelDef.authoritiesDef?.let {
            AuthorityEnumRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render StringTypes`() {

        this.modelDef.stringTypeDefs.forEach {

            if (it.isNotProvided) {

                StringTypeRenderer(it).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render BooleanTypes`() {

        this.modelDef.booleanTypeDefs.forEach {
            if (it.isNotProvided) {

                SimpleTypeRenderer(it).renderToDir(this.kotlinOutputDir)

            }
        }

    }


    private fun `render HazelcastEntityConfig`() {

        val cacheableEntityDefs = this.modelDef.entityHierarchies
            .flatMap { it.entityDefs }
            .filter { it.cacheableDef != null }

        HazelcastEntityConfigRenderer(cacheableEntityDefs, this.modelDef.hazelcastEntityConfigClassDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render IdAndNameDtos`() {

        this.modelDef.entityHierarchies.map { it.entityDef }.filter { it.hasIdAndNameDtoDef }.forEach { entityDef ->
            DtoRenderer(entityDef.entityIdAndNameDef.dtoDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render IntTypes`() {

        this.modelDef.intTypeDefs.forEach {

            if (it.isNotProvided) {

                SimpleTypeRenderer(it).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render LongTypes`() {

        this.modelDef.longTypeDefs.forEach {

            if (it.isNotProvided) {

                SimpleTypeRenderer(it).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render EntityDetailDtos`() {

        this.modelDef.entityDetailDtoDefs.forEach {
            it.let { DtoRenderer(it.dtoDef).renderToDir(kotlinOutputDir) }
        }

    }


    private fun `render enums`() {

        this.modelDef.enumDefs.filter { enumDef -> enumDef.isProvided == false }.forEach {
            EnumRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `process entities`() {

        this.modelDef.entityHierarchies.forEach { `process entity`(it) }

    }


    private fun `process entity`(entityHierarchy: EntityHierarchy) {

        `render entity`(entityHierarchy)
        `render entity primary key`(entityHierarchy)
        `render EntityMeta`(entityHierarchy)
        `render entity FieldConverter interface`(entityHierarchy)
        `render EntityFilter interface`(entityHierarchy)
        `render EntityFilters`(entityHierarchy)
        `render EntityUpdater`(entityHierarchy)
        `render Hazelcast Serializer`(entityHierarchy)
        entityHierarchy.entityDefs.filter { it.isStagingEntity }.forEach { renderStagingEntityExtension(it) }

    }


    private fun `render entity`(entityHierarchy: EntityHierarchy) {

        EntityRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)

    }


    private fun `render entity primary key`(entityHierarchy: EntityHierarchy) {

        val entityDef = entityHierarchy.entityDef

        if (entityDef.hasCompositePrimaryKey) {
            EntityPkRenderer(entityDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render EntityMeta`(entityHierarchy: EntityHierarchy) {

        EntityMetaRenderer(entityHierarchy).renderToDir(this.kotlinOutputDir)

    }


    private fun renderStagingEntityExtension(entityDef: EntityDef) {

        StagingEntityExtensionsRenderer(entityDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render entity FieldConverter interface`(entityHierarchy: EntityHierarchy) {

        val renderer = EntityFieldConverterRenderer(entityHierarchy.entityDef)
        renderer.renderToDir(this.kotlinOutputDir)

    }


    private fun `render EntityFilter interface`(entityHierarchy: EntityHierarchy) {

        EntityFilterRenderer(entityHierarchy.entityDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EntityFilters`(entityHierarchy: EntityHierarchy) {

        EntityFiltersRenderer(entityHierarchy.entityDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EntityUpdater`(entityHierarchy: EntityHierarchy) {

        val entityDef = entityHierarchy.entityDef

        if (entityDef.hasModifiableFields()) {

            EntityUpdaterRenderer(entityDef).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render Hazelcast Serializer`(entityHierarchy: EntityHierarchy) {

        val entityDef = entityHierarchy.entityDef

        entityDef.cacheableDef?.let {
            HazelcastSerializerRenderer(
                it,
                entityDef.entityClassDef,
                entityDef.hazelcastSerializerClassDef
            ).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render data classes`() {

        this.modelDef.dataClassDefs.forEach {
            DataClassRenderer(it).renderToDir(this.kotlinOutputDir)

            it.cacheableDef?.let { cacheableDef ->

                HazelcastSerializerRenderer(
                    cacheableDef,
                    it.classDef,
                    it.hazelcastSerializerClassDef
                ).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render form models`() {

        this.modelDef.formModelDefs.forEach { this.renderFormModel(it) }

    }


    private fun `render RequestDtos`() {

        this.modelDef.requestDtoDefs.forEach { this.processRequestDto(it) }

    }


    private fun `render async validation DTOs`() {

        this.modelDef.entityHierarchies.flatMap { it.entityDefs }.forEach { renderAsyncValidationDto(it) }

    }


    private fun renderAsyncValidationDto(entityDef: EntityDef) {

        entityDef.uniqueIndexDefs.filter { it.withExistsEndpoint }.forEach { entityIndexDef ->

            val dtoBaseName = entityIndexDef.asyncValidator.asyncValidationDtoBaseName
            val dtoSuffix = entityIndexDef.asyncValidator.asyncValidationDtoSuffix

            val classFieldDefs = entityIndexDef.indexDef.entityFieldDefs.map { field ->
                aClassField(field.classFieldName, field.classFieldDef.fieldType).build()
            }.map { RequestDtoFieldDef(it, null) }

            val requestDtoDef = RequestDtoDef(
                dtoBaseName = dtoBaseName,
                dtoSuffix = dtoSuffix,
                packageName = entityDef.packageName,
                moduleName = null,
                dtoFieldDefs = classFieldDefs,
                withGeneratedEndpoint = WithGeneratedEndpoint.FALSE,
                preAuthorizeExpression = null
            )

            RequestDtoRenderer(requestDtoDef).renderToDir(this.kotlinOutputDir)

        }

    }


    private fun `render ResponseDtos`() {

        this.modelDef.responseDtoDefs.forEach { this.processResponseDto(it) }

    }


    private fun `render FetchForEditDtos`() {

        this.modelDef.fetchForEditDtoDefs.forEach { DtoRenderer(it.dtoDef).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render SimpleResponseDtos`() {

        this.modelDef.simpleResponseDtoDefs.forEach { this.processSimpleResponseDto(it) }

    }


    private fun `render SearchableDtos`() {

        this.modelDef.allSearchableDtoDefs.forEach { processSearchableDto(it) }

    }


    private fun `render TableDtos`() {

        this.modelDef.dtoHtmlTableDefs.filter { it.withGeneratedDto.value }.forEach { processDtoHtmlTableDef(it) }

    }


    private fun `process CrudApiDefs`() {

        this.modelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete }
            .forEach { processCrudApiDef(it) }

    }


    private fun processCrudApiDef(entityCrudApiDef: EntityCrudApiDef) {

        entityCrudApiDef.createApiDef?.let { renderRequestDto(it.requestDtoDef) }
        entityCrudApiDef.updateApiDef?.let {
            renderRequestDto(it.requestDtoDef)
            renderInlineEditDtos(it.inlineEditDtoDefs)
        }

    }


    private fun processRequestDto(requestDtoDef: RequestDtoDef) {

        renderRequestDto(requestDtoDef)

    }


    private fun processResponseDto(responseDtoDef: ResponseDtoDef) {

        renderResponseDtoFieldNameConverter(responseDtoDef)
        renderResponseDtoFieldConverter(responseDtoDef)
        renderResponseDtoSearchParser(responseDtoDef)
        renderResponseDto(responseDtoDef)
        renderResponseDtoCsvHelper(responseDtoDef)

    }


    private fun processSimpleResponseDto(responseDtoDef: SimpleResponseDtoDef) {

//        renderResponseDtoFieldNameConverter(responseDtoDef)
//        renderResponseDtoFieldConverter(responseDtoDef)
//        renderResponseDtoSearchParser(responseDtoDef)
        renderSimpleResponseDto(responseDtoDef)
//        renderResponseDtoCsvHelper(responseDtoDef)

    }


    private fun processSearchableDto(searchableDtoDef: SearchableDtoDef) {

        if (searchableDtoDef.withGeneratedDto.value) {
            renderDto(searchableDtoDef.dtoDef)
        }

        renderSearchableDtoMeta(searchableDtoDef)
        renderAgGridDtoFieldConverter(searchableDtoDef)
        renderAgGridDtoFieldNameConverter(searchableDtoDef)

    }


    private fun renderSearchableDtoMeta(searchableDtoDef: SearchableDtoDef) {

        SearchableDtoMetaRenderer(searchableDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun processDtoHtmlTableDef(dtoHtmlTableDef: DtoHtmlTableDef) {

        renderDto(dtoHtmlTableDef.dtoDef)

    }


    private fun renderRequestDto(requestDtoDef: RequestDtoDef) {

        RequestDtoRenderer(requestDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderFormModel(formModelDef: FormModelDef) {

        FormModelRenderer(formModelDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderResponseDtoFieldNameConverter(responseDtoDef: ResponseDtoDef) {

        val renderer = SearchRequestFieldNameConverterRenderer(
            responseDtoDef.searchRequestFieldNameConverterClassDef,
            responseDtoDef.allFields
        )
        renderer.renderToDir(this.kotlinOutputDir)

    }


    private fun renderAgGridDtoFieldNameConverter(searchableDtoDef: SearchableDtoDef) {

        SearchRequestFieldNameConverterRenderer(
            searchableDtoDef.fieldNameConverterClassDef,
            searchableDtoDef.allFields.map { it.responseDtoFieldDef }
        ).renderToDir(this.kotlinOutputDir)

    }


    private fun renderResponseDtoFieldConverter(responseDtoDef: ResponseDtoDef) {

        if (responseDtoDef.hasProvidedFieldConverter()) {
            return
        }

        SearchRequestFieldConverterRenderer(
            responseDtoDef.searchRequestFieldConverterClassDef,
            responseDtoDef.allFields
        ).renderToDir(this.kotlinOutputDir)

    }


    private fun renderAgGridDtoFieldConverter(searchableDtoDef: SearchableDtoDef) {

        if (searchableDtoDef.hasProvidedFieldConverter()) {
            return
        }

        SearchRequestFieldConverterRenderer(
            searchableDtoDef.fieldConverterClassDef,
            searchableDtoDef.allFields.map { it.responseDtoFieldDef }
        ).renderToDir(this.kotlinOutputDir)

    }


    private fun renderResponseDtoSearchParser(responseDtoDef: ResponseDtoDef) {

        SearchRequestSearchParserRenderer(responseDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderResponseDto(responseDtoDef: ResponseDtoDef) {

        renderDto(responseDtoDef.dtoDef)

    }


    private fun renderSimpleResponseDto(responseDtoDef: SimpleResponseDtoDef) {

        renderDto(responseDtoDef.dtoDef)

    }


    private fun renderDto(dtoDef: ClassDef) {

        DtoRenderer(dtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderInlineEditDtos(inlineEditDtoDefs: List<InlineEditDtoDef>) {

        inlineEditDtoDefs.forEach { inlineEditDtoDef -> renderRequestDto(inlineEditDtoDef.requestDtoDef) }

    }


    private fun renderResponseDtoCsvHelper(responseDtoDef: ResponseDtoDef) {

        ResponseDtoCsvHelperRenderer(responseDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EsDocs`() {

        val allEsDocs = this.modelDef.allEsDocDefs

        allEsDocs.forEach {
            renderEsDoc(it)
        }

    }


    private fun renderEsDoc(esDocDef: EsDocDef) {

        DtoRenderer(esDocDef.dtoDef).renderToDir(this.kotlinOutputDir)

        esDocDef.fieldEnumDef?.let { enumDef ->
            EnumRenderer(enumDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `process HazelcastDtoDefs`() {

        this.modelDef.hazelcastDtoDefs.forEach {
            renderHzDto(it)
            renderHzSerializer(it)
        }

    }


    private fun renderHzDto(hazelcastDtoDef: HazelcastDtoDef) {

        DtoRenderer(hazelcastDtoDef.dtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderHzSerializer(hazelcastDtoDef: HazelcastDtoDef) {

        HazelcastSerializerRenderer(
            hazelcastDtoDef.cacheableDef,
            hazelcastDtoDef.dtoDef,
            hazelcastDtoDef.serializerClassDef
        ).renderToDir(this.kotlinOutputDir)

    }


    private fun `render CrudListeners`() {

        this.modelDef.entityCrudApiDefs.forEach { entityCrudApiDef ->
            CrudListenerRenderer(entityCrudApiDef).renderToDir(this.kotlinOutputDir)
        }

    }


}
