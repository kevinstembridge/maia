package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef.Companion.aClassField
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class EntityIdAndNameDef(
    val packageName: PackageName,
    val dtoBaseName: DtoBaseName,
    val idEntityFieldDef: EntityFieldDef,
    val nameEntityFieldDef: EntityFieldDef,
    val entityRepoClassDef: ClassDef
) {


    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName.withSuffix("IdAndName"),
        DtoSuffix("Dto"),
        listOf(
            aClassField(idEntityFieldDef.classFieldName.value, idEntityFieldDef.fieldType).build(),
            aClassField("name", nameEntityFieldDef.fieldType).build(),
        )
    ).build()


    val idAndNameDtoFqcn = dtoDef.fqcn


    val dtoUqcn = dtoDef.uqcn


    val idAndNameDtoImportStatement = "import { ${idAndNameDtoFqcn.uqcn} } from '@${GeneratedTypescriptDir.forPackage(idAndNameDtoFqcn.packageName)}/${idAndNameDtoFqcn.uqcn}';"


    val idAndNameDtoTypescriptImport = TypescriptImport(idAndNameDtoFqcn.uqcn.value, "@${GeneratedTypescriptDir.forPackage(idAndNameDtoFqcn.packageName)}/${idAndNameDtoFqcn.uqcn}")


}
