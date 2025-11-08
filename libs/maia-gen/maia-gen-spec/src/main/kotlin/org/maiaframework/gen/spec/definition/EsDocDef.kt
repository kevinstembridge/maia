package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.builders.EnumDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import org.maiaframework.gen.spec.definition.lang.Uqcn
import org.maiaframework.lang.text.StringFunctions.padWithLeadingZeroes

class EsDocDef(
    val packageName: PackageName,
    val esDocBaseName: DtoBaseName,
    val elasticIndexBaseName: ElasticIndexBaseName,
    val esDocVersion: Int,
    val indexDescription: Description,
    val fields: List<EsDocFieldDef>,
    renderFieldEnum: Boolean,
    val generateRefreshIndexJob: Boolean,
    val disableRendering: Boolean,
    val entityDef: EntityDef?
) {


    private val dtoSuffix = DtoSuffix("EsDoc")


    private val esDocBaseNameWithVersion = esDocBaseName.withSuffix("V$esDocVersion")


    private val allClassFields = fields.map { it.classFieldDef }


    val dtoDef = DtoDefBuilder(packageName, esDocBaseNameWithVersion, dtoSuffix, allClassFields)
//    val dtoDef = DtoDefBuilder(packageName, esDocBaseName, dtoSuffix, allClassFields)
        .withCharacteristic(DtoCharacteristic.ELASTIC_SEARCH_DOC, DtoCharacteristic.RESPONSE_DTO)
        .build()


    val uqcn = dtoDef.uqcn


    val fqcn = dtoDef.fqcn


    private val esDocMetaClassFqcn = Fqcn.valueOf(
        packageName,
        Uqcn(esDocBaseName.value).withSuffix("EsIndexMeta_v${padWithLeadingZeroes(this.esDocVersion, 4)}")
    )


    private val esDocIndexControlFqcn = Fqcn.valueOf(
        packageName,
        Uqcn(esDocBaseName.value).withSuffix("EsIndexControl_v${padWithLeadingZeroes(this.esDocVersion, 4)}")
    )


    val esIndexControlClassDef: ClassDef = aClassDef(esDocIndexControlFqcn)
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withSuperclass(
            aClassDef(ParameterizedType(Fqcns.ABSTRACT_ES_INDEX_CONTROL))
                .withFieldDefsNotInherited(
                    listOf(
                        aClassField("esIndexNameProvider", FieldTypes.byFqcn(Fqcns.ES_INDEX_NAME_OVERRIDER)).build(),
                        aClassField("esIndexActiveVersionManager", FieldTypes.byFqcn(Fqcns.ES_INDEX_ACTIVE_VERSION_MANAGER)).build(),
                        aClassField("client", FieldTypes.byFqcn(Fqcns.ELASTIC_CLIENT)).build()
                    )
                ).build()
        )
        .build()


    val refreshEsIndexJobName = "refresh${esDocBaseName}Index"


    val indexServiceClassDef = aClassDef(packageName.uqcn("${esDocBaseName}IndexService"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val esIndexClassDef = aClassDef(packageName.uqcn("${esDocBaseName}EsIndex"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val refreshIndexJobClassDef = aClassDef(packageName.uqcn("Refresh${esDocBaseName.firstToUpper()}IndexJob"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedType(Fqcns.MAHANA_JOB))
        .build()


    fun findFieldByPath(fieldName: String): ClassFieldDef {
        return this.dtoDef.findFieldByPath(fieldName)
    }


    val esDocMapperClassDef = aClassDef(this.dtoDef.fqcn.withSuffix("Mapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedType(Fqcns.MAHANA_ES_DOC_MAPPER, ParameterizedType(dtoDef.fqcn)))
        .build()


    val esDocRepoClassDef = aClassDef(this.dtoDef.fqcn.withSuffix("Repo"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


    val esDocMetaClassDef = aClassDef(esDocMetaClassFqcn)
        .ofType(ClassType.OBJECT)
        .build()


    val fieldEnumDef = if (renderFieldEnum) {
        val builder = EnumDefBuilder(packageName.uqcn(this.dtoDef.uqcn.withSuffix("Field")))
        fields.forEach { builder.value(it.classFieldDef.classFieldName.value) }
        builder.build()
    } else {
        null
    }


}
