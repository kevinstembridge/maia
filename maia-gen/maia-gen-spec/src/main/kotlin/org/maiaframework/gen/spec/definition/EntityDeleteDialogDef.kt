package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.flags.Deletable

class EntityDeleteDialogDef(
    entityDef: EntityDef
) {


    init {

        check(entityDef.deletable == Deletable.TRUE) { "Entity ${entityDef.entityBaseName} is not deletable" }

    }


    private val angularComponentNames =
        AngularComponentNames(entityDef.packageName, "${entityDef.entityBaseName}DeleteDialog")


    val angularDialogComponentName = angularComponentNames.componentName


    val angularDialogComponentHtmlFileName = angularComponentNames.htmlFileName


    val angularDialogComponentSelector = angularComponentNames.componentSelector


    val dialogComponentRenderedFilePath = angularComponentNames.componentRenderedFilePath


    val dialogHtmlRenderedFilePath = angularComponentNames.htmlRenderedFilePath


    val angularDialogComponentImportStatement = angularComponentNames.componentImportStatement


    val angularDialogComponentTypescriptImport = angularComponentNames.componentTypescriptImport


    val checkForeignKeyReferencesDialogComponentName = entityDef.checkForeignKeyReferencesDialogComponentNames.componentName


}
