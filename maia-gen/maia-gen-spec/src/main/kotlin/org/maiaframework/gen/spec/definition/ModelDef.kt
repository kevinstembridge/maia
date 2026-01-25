package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.ClassDef


class ModelDef(
    val appKey: AppKey,
    val rootEntityHierarchies: List<EntityHierarchy>,
    val authoritiesDef: AuthoritiesDef?,
    val formModelDefs: List<FormModelDef>,
    val entityHtmlFormDefs: List<EntityHtmlFormDef>,
    val requestDtoDefs: List<RequestDtoDef>,
    val responseDtoDefs: List<ResponseDtoDef>,
    val simpleResponseDtoDefs: List<SimpleResponseDtoDef>,
    val hazelcastDtoDefs: List<HazelcastDtoDef>,
    val searchableDtoDefs: List<SearchableDtoDef>,
    val dtoHtmlTableDefs: List<DtoHtmlTableDef>,
    val requestDtoHtmlFormDefs: List<HtmlFormDef>,
    val angularFormDefs: List<AngularFormDef>,
    val dataClassDefs: List<DataClassDef>,
    val enumDefs: List<EnumDef>,
    val booleanTypeDefs: List<BooleanTypeDef>,
    val intTypeDefs: List<IntTypeDef>,
    val longTypeDefs: List<LongTypeDef>,
    val stringTypeDefs: List<StringTypeDef>,
    val typeaheadDefs: List<TypeaheadDef>,
    val crudTableDefs: List<CrudTableDef>,
    val esDocsDefs: List<EsDocDef>,
    val hazelcastEntityConfigClassDef: ClassDef
) {


    val entityHierarchies: List<EntityHierarchy> = this.rootEntityHierarchies.flatMap { it.entityHierarchies }


    private val entityDefs = rootEntityHierarchies.flatMap { it.entityDefs }


    val allSearchableDtoDefs = dtoHtmlTableDefs.mapNotNull { it.searchableDtoDef }.plus(searchableDtoDefs)


    val entityCrudApiDefs = entityDefs.mapNotNull { it.entityCrudApiDef }


    val fetchForEditDtoDefs = entityDefs.filter { it.isConcrete }.mapNotNull { it.fetchForEditDtoDef }


    val entityDetailDtoDefs = entityDefs.filterNot { it.isHistoryEntity }.mapNotNull { it.entityDetailDtoDef }


    val entitiesReferencedByForeignKey = entityDefs
        .filter { it.deletable.value }
        .flatMap { it.allForeignKeyEntityFieldDefs }
        .mapNotNull { it.foreignKeyFieldDef?.foreignEntityDef }
        .toSet()


    fun entityIsReferencedByForeignKeys(entityDef: EntityDef): Boolean {
        return entitiesReferencedByForeignKey.contains(entityDef)
    }


    fun entitiesThatReference(referencedEntityDef: EntityDef): List<EntityDef> {

        return this.entityDefs.filter { entityDef: EntityDef ->

            entityDef.allForeignKeyEntityFieldDefs.mapNotNull { it.foreignKeyFieldDef?.foreignEntityDef }.contains(referencedEntityDef)

        }

    }


    fun entityHierarchyFor(entityDef: EntityDef): EntityHierarchy {

        return this.entityHierarchies.find { it.entityDef == entityDef }
            ?: throw IllegalArgumentException("No EntityHierarchy found for EntityDef $entityDef")

    }


    val allEsDocDefs = typeaheadDefs.map { it.esDocDef }.plus(esDocsDefs)


}
