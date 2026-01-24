package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class EntityPkAndNameDef(
    val packageName: PackageName,
    val dtoBaseName: DtoBaseName,
    val pkEntityFieldDef: EntityFieldDef,
    val nameEntityFieldDef: EntityFieldDef,
    val entityRepoClassDef: ClassDef
) {


    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName.withSuffix("PkAndName"),
        DtoSuffix("Dto"),
        listOf(
            aClassField(pkEntityFieldDef.classFieldName.value, pkEntityFieldDef.fieldType).build(),
            aClassField("name", nameEntityFieldDef.fieldType).build(),
        )
    ).build()


    val pkAndNameDtoFqcn = dtoDef.fqcn


    val dtoUqcn = dtoDef.uqcn


    val pkAndNameDtoImportStatement = "import { ${pkAndNameDtoFqcn.uqcn} } from '@${GeneratedTypescriptDir.forPackage(pkAndNameDtoFqcn.packageName)}/${pkAndNameDtoFqcn.uqcn}';"


    val pkAndNameDtoTypescriptImport = TypescriptImport(pkAndNameDtoFqcn.uqcn.value, "@${GeneratedTypescriptDir.forPackage(pkAndNameDtoFqcn.packageName)}/${pkAndNameDtoFqcn.uqcn}")


}
