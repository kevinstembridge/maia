package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.*
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.*

class DtoDefBuilder(
    private val packageName: PackageName,
    val dtoBaseName: DtoBaseName,
    private val dtoSuffix: DtoSuffix,
    private val classFields: List<ClassFieldDef>
) {


    private val characteristics = mutableSetOf<DtoCharacteristic>()


    fun withCharacteristic(vararg characteristics: DtoCharacteristic): DtoDefBuilder {

        this.characteristics.addAll(characteristics)
        return this

    }


    fun build(): ClassDef {

        val annotationDefs = determineClassAnnotations()

        return aClassDef(packageName.uqcn(dtoBaseName.withSuffix(dtoSuffix).value))
            .ofType(ClassType.DATA_CLASS)
            .withClassAnnotation(*annotationDefs)
            .withFieldDefsNotInherited(classFields)
            .build()

    }


    private fun determineClassAnnotations(): Array<AnnotationDef> {

        val annotationDefs = mutableSetOf<AnnotationDef>()

          // TODO Unable to use JsonInclude.NON_EMPTY due to a bug in Jackson 3
          // https://github.com/FasterXML/jackson-module-kotlin/issues/1065
//        if (this.characteristics.contains(DtoCharacteristic.RESPONSE_DTO)) {
//            annotationDefs.add(AnnotationDefs.JSON_INCLUDE)
//        }

        return annotationDefs.toTypedArray()

    }


}
