package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.cache.CacheName
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder
import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType


class HazelcastDtoDef(
    val dtoBaseName: DtoBaseName,
    dtoSuffix: DtoSuffix,
    packageName: PackageName,
    classFieldDefs: List<ClassFieldDef>,
    vararg dtoCharacteristic: DtoCharacteristic
) {


    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName,
        dtoSuffix,
        classFieldDefs.sorted()
    )
        .withCharacteristic(*dtoCharacteristic)
        .build()


    val serializerClassDef = ClassDefBuilder.aClassDef(packageName.uqcn(dtoBaseName.withSuffix("Serializer").value))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedTypes.hazelcastCompactSerializer(ParameterizedType(dtoDef.fqcn)))
        .build()


    val cacheableDef = CacheableDef(CacheName(dtoBaseName.value))


}
