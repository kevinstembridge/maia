package org.maiaframework.gen.spec

import org.maiaframework.gen.spec.definition.AnnotationDefs
import org.maiaframework.gen.spec.definition.ApplicationModelDef
import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.DisplayName
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.EnumValueDef
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.PackageName

abstract class ApplicationSpec(
    defaultPackageName: String,
    private val hazelcastConfigRequiresSpringComponentAnnotation: Boolean = true
) : ApplicationModelDefProvider {


    protected val defaultPackageName = PackageName(defaultPackageName)


    protected abstract val modelDefs: List<ModelDef>


    override val applicationModelDef: ApplicationModelDef by lazy {

        ApplicationModelDef(
            angularFormDefs = this.modelDefs.flatMap { it.angularFormDefs },
            authoritiesDef = buildAuthoritiesDef(),
            blotterDefs = this.modelDefs.flatMap { it.blotterDefs },
            blotterPageDefs = this.modelDefs.flatMap { it.blotterPageDefs },
            booleanTypeDefs = this.modelDefs.flatMap { it.booleanTypeDefs },
            dataClassDefs = this.modelDefs.flatMap { it.dataClassDefs },
            entityCreatePageDefs = this.modelDefs.flatMap { it.entityCreatePageDefs },
            entityDetailViewDefs = this.modelDefs.flatMap { it.entityDetailViewDefs },
            entityEditPageDefs = this.modelDefs.flatMap { it.entityEditPageDefs },
            entityHtmlFormDefs = this.modelDefs.flatMap { it.entityHtmlFormDefs },
            enumDefs = this.modelDefs.flatMap { it.enumDefs },
            esDocsDefs = this.modelDefs.flatMap { it.esDocsDefs },
            formModelDefs = this.modelDefs.flatMap { it.formModelDefs },
            hazelcastDtoDefs = this.modelDefs.flatMap { it.hazelcastDtoDefs },
            hazelcastEntityConfigClassDef = buildHazelcastConfigClassDef(),
            intTypeDefs = this.modelDefs.flatMap { it.intTypeDefs },
            longTypeDefs = this.modelDefs.flatMap { it.longTypeDefs },
            requestDtoDefs = this.modelDefs.flatMap { it.requestDtoDefs },
            requestDtoHtmlFormDefs = this.modelDefs.flatMap { it.requestDtoHtmlFormDefs },
            responseDtoDefs = this.modelDefs.flatMap { it.responseDtoDefs },
            rootEntityHierarchies = this.modelDefs.flatMap { it.rootEntityHierarchies },
            rowMapperDefs = this.modelDefs.flatMap { it.rowMapperDefs },
            searchableDtoDefs = this.modelDefs.flatMap { it.allSearchableDtoDefs },
            simpleResponseDtoDefs =  this.modelDefs.flatMap { it.simpleResponseDtoDefs },
            stringTypeDefs = this.modelDefs.flatMap { it.stringTypeDefs },
            typeaheadDefs = this.modelDefs.flatMap { it.typeaheadDefs }
        )

    }


    private fun buildHazelcastConfigClassDef(): ClassDef {

        val fqcn = this.defaultPackageName.plusSubPackage("hazelcast").uqcn("HazelcastConfig")
        val classDefBuilder = aClassDef(fqcn)

        if (this.hazelcastConfigRequiresSpringComponentAnnotation) {
            classDefBuilder.withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        }

        return classDefBuilder.build()

    }


    private fun buildAuthoritiesDef(): AuthoritiesDef? {

        val allAuthorities = this.modelDefs.flatMap { it.authorityDefs }

        if (allAuthorities.isEmpty()) {
            return null
        }

        val enumValueDefs = allAuthorities.map {
            EnumValueDef(
                name = it.name,
                description = it.description,
                displayName = DisplayName(it.name),
                isDefaultFormValue = false
            )
        }

        val enumDef = EnumDef(
            defaultPackageName.uqcn("Authority"),
            enumValueDefs,
            isProvided = false,
            withTypescript = true,
            withEnumSelectionOptions = true
        )

        val objectClassDef = aClassDef(enumDef.fqcn).ofType(ClassType.OBJECT).build()

        return AuthoritiesDef(enumDef)

    }


}
