package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.AuthorityEnumRenderer
import org.maiaframework.gen.renderers.CrudListenerRenderer
import org.maiaframework.gen.renderers.DataClassRenderer
import org.maiaframework.gen.renderers.DtoRenderer
import org.maiaframework.gen.renderers.EntityFieldConverterRenderer
import org.maiaframework.gen.renderers.EntityFilterRenderer
import org.maiaframework.gen.renderers.EntityFiltersRenderer
import org.maiaframework.gen.renderers.EntityHistoryBlotterRowDtoMetaRenderer
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
import org.maiaframework.gen.renderers.TimelineBlotterRowDtoDomainRenderer
import org.maiaframework.gen.renderers.TimelineBlotterRowDtoMetaRenderer
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.EntityCrudApiDef
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.EsDocDef
import org.maiaframework.gen.spec.definition.FormModelDef
import org.maiaframework.gen.spec.definition.HazelcastDtoDef
import org.maiaframework.gen.spec.definition.InlineEditDtoDef
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
        val moduleGenerator = DomainModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
        moduleGenerator.generateSource(moduleGeneratorFixture.applicationModelDef)

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class DomainModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
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
        `render JoinFetchDtos`()
        `render form models`()
        `render HazelcastEntityConfig`()
        `render PkAndNameDtos`()
        `render IntTypes`()
        `render LongTypes`()
        `render RequestDtos`()
        `render ResponseDtos`()
        `render SearchableDtos`()
        `render SimpleResponseDtos`()
        `render StringTypes`()
        `render blotter DTOs`()
        `render CrudListeners`()
        `render EntityHistoryBlotterDomainArtifacts`()
        `render TimelineBlotterDomainArtifacts`()

    }


    private fun `render Authority enum`() {

        this.applicationModelDef.authoritiesDef?.let {
            AuthorityEnumRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render StringTypes`() {

        this.applicationModelDef.stringTypeDefs
            .filter { it.isNotProvided }
            .forEach { StringTypeRenderer(it).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render BooleanTypes`() {

        this.applicationModelDef.booleanTypeDefs
            .filter { it.isNotProvided }
            .forEach { SimpleTypeRenderer(it).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render HazelcastEntityConfig`() {

        val cacheableEntityDefs = this.applicationModelDef.entityHierarchies
            .flatMap { it.entityDefs }
            .filter { it.cacheableDef != null }

        HazelcastEntityConfigRenderer(cacheableEntityDefs, this.applicationModelDef.hazelcastEntityConfigClassDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render PkAndNameDtos`() {

        this.applicationModelDef.entityHierarchies
            .map { it.entityDef }
            .filter { it.hasPkAndNameDtoDef }
            .map { it.entityPkAndNameDef.dtoDef }
            .forEach { renderDto(it) }

    }


    private fun `render IntTypes`() {

        this.applicationModelDef.intTypeDefs
            .filter { it.isNotProvided }
            .forEach { SimpleTypeRenderer(it).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render LongTypes`() {

        this.applicationModelDef.longTypeDefs
            .filter { it.isNotProvided }
            .forEach { SimpleTypeRenderer(it).renderToDir(this.kotlinOutputDir) }

    }


    private fun `render EntityDetailDtos`() {

        this.applicationModelDef.entityDetailViewDefs.forEach { renderDto(it.dtoDef) }

    }


    private fun `render enums`() {

        this.applicationModelDef.enumDefs.filter { enumDef -> enumDef.isProvided == false }.forEach {
            EnumRenderer(it).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `process entities`() {

        this.applicationModelDef.entityHierarchies.forEach { `process entity`(it) }

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

        EntityFieldConverterRenderer(entityHierarchy.entityDef).renderToDir(this.kotlinOutputDir)

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
                it.cacheName,
                entityDef.entityClassDef,
                entityDef.hazelcastSerializerClassDef
            ).renderToDir(this.kotlinOutputDir)

            if (entityDef.hasCompositePrimaryKey) {

                HazelcastSerializerRenderer(
                    it.cacheName.withSuffix("_pk"),
                    entityDef.entityPkClassDef,
                    entityDef.hazelcastPrimaryKeySerializerClassDef
                ).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render data classes`() {

        this.applicationModelDef.dataClassDefs.forEach {
            DataClassRenderer(it).renderToDir(this.kotlinOutputDir)

            it.cacheableDef?.let { cacheableDef ->

                HazelcastSerializerRenderer(
                    cacheableDef.cacheName,
                    it.classDef,
                    it.hazelcastSerializerClassDef
                ).renderToDir(this.kotlinOutputDir)

            }

        }

    }


    private fun `render form models`() {

        this.applicationModelDef.formModelDefs.forEach { this.renderFormModel(it) }

    }


    private fun `render RequestDtos`() {

        this.applicationModelDef.requestDtoDefs.forEach { this.processRequestDto(it) }

    }


    private fun `render async validation DTOs`() {

        this.applicationModelDef.entityHierarchies.flatMap { it.entityDefs }.forEach { renderAsyncValidationDto(it) }

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

        this.applicationModelDef.responseDtoDefs.forEach { this.processResponseDto(it) }

    }


    private fun `render FetchForEditDtos`() {

        this.applicationModelDef.fetchForEditDtoDefs.forEach { renderDto(it.dtoDef) }

    }


    private fun `render JoinFetchDtos`() {

        this.applicationModelDef.joinFetchDtoDefs.forEach { renderDto(it.dtoDef) }

    }


    private fun `render SimpleResponseDtos`() {

        this.applicationModelDef.simpleResponseDtoDefs.forEach { this.processSimpleResponseDto(it) }

    }


    private fun `render SearchableDtos`() {

        this.applicationModelDef.allSearchableDtoDefs.forEach { processSearchableDto(it) }

    }


    private fun `render blotter DTOs`() {

        this.applicationModelDef.blotterDefs
            .filter { it.withGeneratedDto.value }
            .forEach { processBlotterDef(it) }

    }


    private fun `process CrudApiDefs`() {

        this.applicationModelDef.entityCrudApiDefs
            .filter { it.entityDef.isConcrete }
            .forEach { processCrudApiDef(it) }

    }


    private fun processCrudApiDef(entityCrudApiDef: EntityCrudApiDef) {

        entityCrudApiDef.createApiDef?.let { createApiDef ->
            createApiDef.manyToManyTimestampedJoinRequestDtoDefs.forEach { renderRequestDto(it) }
            renderRequestDto(createApiDef.requestDtoDef)
        }
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


    private fun processBlotterDef(blotterDef: BlotterDef) {

        renderDto(blotterDef.dtoDef)

    }


    private fun renderRequestDto(requestDtoDef: RequestDtoDef) {

        RequestDtoRenderer(requestDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderFormModel(formModelDef: FormModelDef) {

        FormModelRenderer(formModelDef).renderToDir(this.kotlinOutputDir)

    }


    private fun renderResponseDtoFieldNameConverter(responseDtoDef: ResponseDtoDef) {

        SearchRequestFieldNameConverterRenderer(
            responseDtoDef.searchRequestFieldNameConverterClassDef,
            responseDtoDef.allFields
        ).renderToDir(this.kotlinOutputDir)

    }


    private fun renderAgGridDtoFieldNameConverter(searchableDtoDef: SearchableDtoDef) {

        SearchRequestFieldNameConverterRenderer(
            searchableDtoDef.fieldNameConverterClassDef,
            searchableDtoDef.nonManyToManyFields.map { it.responseDtoFieldDef }
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
            searchableDtoDef.nonManyToManyFields.map { it.responseDtoFieldDef }
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


    private fun renderInlineEditDtos(inlineEditDtoDefs: List<InlineEditDtoDef>) {

        inlineEditDtoDefs.forEach { inlineEditDtoDef -> renderRequestDto(inlineEditDtoDef.requestDtoDef) }

    }


    private fun renderResponseDtoCsvHelper(responseDtoDef: ResponseDtoDef) {

        ResponseDtoCsvHelperRenderer(responseDtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EsDocs`() {

        val allEsDocs = this.applicationModelDef.allEsDocDefs

        allEsDocs.forEach {
            renderEsDoc(it)
        }

    }


    private fun renderEsDoc(esDocDef: EsDocDef) {

        renderDto(esDocDef.dtoDef)

        esDocDef.fieldEnumDef?.let { enumDef ->
            EnumRenderer(enumDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `process HazelcastDtoDefs`() {

        this.applicationModelDef.hazelcastDtoDefs.forEach {
            renderHzDto(it)
            renderHzSerializer(it)
        }

    }


    private fun renderHzDto(hazelcastDtoDef: HazelcastDtoDef) {

        renderDto(hazelcastDtoDef.dtoDef)

    }


    private fun renderHzSerializer(hazelcastDtoDef: HazelcastDtoDef) {

        HazelcastSerializerRenderer(
            hazelcastDtoDef.cacheableDef.cacheName,
            hazelcastDtoDef.dtoDef,
            hazelcastDtoDef.serializerClassDef
        ).renderToDir(this.kotlinOutputDir)

    }


    private fun `render CrudListeners`() {

        this.applicationModelDef.entityCrudApiDefs.forEach { entityCrudApiDef ->
            CrudListenerRenderer(entityCrudApiDef).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun renderDto(dtoDef: ClassDef) {

        DtoRenderer(dtoDef).renderToDir(this.kotlinOutputDir)

    }


    private fun `render EntityHistoryBlotterDomainArtifacts`() {

        this.applicationModelDef.entityHistoryBlotterDefs.forEach { def ->
            DtoRenderer(def.rowDtoClassDef).renderToDir(this.kotlinOutputDir)
            EntityHistoryBlotterRowDtoMetaRenderer(def).renderToDir(this.kotlinOutputDir)
        }

    }


    private fun `render TimelineBlotterDomainArtifacts`() {

        this.applicationModelDef.timelineBlotterDefs.forEach { def ->
            TimelineBlotterRowDtoDomainRenderer(def).renderToDir(this.kotlinOutputDir)
            TimelineBlotterRowDtoMetaRenderer(def).renderToDir(this.kotlinOutputDir)
        }

    }


}
