package org.maiaframework.gen.generator

import org.maiaframework.gen.renderers.EntityFieldEnumConstantsRenderer
import org.maiaframework.gen.spec.definition.EntityHtmlFormDef
import org.maiaframework.gen.spec.definition.HtmlFormDef
import org.maiaframework.gen.spec.definition.lang.EnumFieldType


fun main(args: Array<String>) {

    try {

        val moduleGeneratorFixture = ModuleGeneratorFixture.from(args)

        moduleGeneratorFixture.modelDefs.forEach {

            val modelGenerator = AppModuleGenerator(moduleGeneratorFixture.maiaGenerationContext)
            modelGenerator.generateSource(it)

        }

    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }

}


class AppModuleGenerator(
    maiaGenerationContext: MaiaGenerationContext
): AbstractModuleGenerator(
    maiaGenerationContext
) {


    override fun onGenerateSource() {

        renderEntityHtmlForms()
        renderRequestDtoHtmlForms()

    }


    private fun renderEntityHtmlForms() {

        this.modelDef.entityHtmlFormDefs.forEach { this.processEntityHtmlFormDef(it) }

    }


    private fun renderRequestDtoHtmlForms() {

        this.modelDef.requestDtoHtmlFormDefs.forEach { this.processRequestDtoHtmlFormDef(it) }

    }


    private fun processEntityHtmlFormDef(formDef: EntityHtmlFormDef) {

        renderAnyEnumConstants(formDef)

    }


    private fun renderAnyEnumConstants(entityHtmlFormDef: EntityHtmlFormDef) {

        entityHtmlFormDef.allHtmlFormFields.forEach { field ->

            val fieldType = field.classFieldDef.fieldType

            if (fieldType is EnumFieldType) {
                val renderer = EntityFieldEnumConstantsRenderer(field.fieldKey.value, fieldType.enumDef)
                renderer.renderToDir(this.typescriptOutputDir)
            }

        }

    }


    private fun processRequestDtoHtmlFormDef(formDef: HtmlFormDef) {

        renderAnyEnumConstants(formDef)

    }


    private fun renderAnyEnumConstants(htmlFormDef: HtmlFormDef) {

        htmlFormDef.allHtmlFormFields.forEach {

            val fieldType = it.classFieldDef.fieldType

            if (fieldType is EnumFieldType) {
                val renderer = EntityFieldEnumConstantsRenderer(it.fieldKey, fieldType.enumDef)
                renderer.renderToDir(this.typescriptOutputDir)
            }

        }

    }


}
