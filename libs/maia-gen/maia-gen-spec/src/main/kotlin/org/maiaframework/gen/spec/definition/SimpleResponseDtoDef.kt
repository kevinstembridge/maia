package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.PackageName


class SimpleResponseDtoDef(
    dtoBaseName: DtoBaseName,
    dtoSuffix: DtoSuffix,
    packageName: PackageName,
    fieldDefsNotInherited: List<SimpleResponseDtoFieldDef>,
    vararg dtoCharacteristic: DtoCharacteristic
) {


    private val allClassFields = fieldDefsNotInherited.sorted().map { it.classFieldDef }


    val dtoDef = DtoDefBuilder(packageName, dtoBaseName, dtoSuffix, allClassFields)
        .withCharacteristic(*dtoCharacteristic)
        .build()


    val fqcn = dtoDef.fqcn


}
