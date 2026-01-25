package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.flags.WithPreAuthorize
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.*
import java.util.*


class FormModelDef(
        val formModelKey: FormModelKey,
        formModelClassName: FormModelClassName,
        packageName: PackageName,
        fieldDefsNotInherited: List<ClassFieldDef>,
        val withPreAuthorize: Optional<WithPreAuthorize>) {


    val classDef: ClassDef
    val allFields: List<ClassFieldDef> = fieldDefsNotInherited
    val handlerClassDef: ClassDef
    val controllerClassDef: ClassDef


    val uqcn: Uqcn
        get() = this.classDef.uqcn


    val fqcn: Fqcn
        get() = this.classDef.fqcn


    init {

        val modelUqcn = Uqcn("${formModelClassName}FormModel")
        val modelFqcn = Fqcn.valueOf(packageName, modelUqcn)
        this.classDef = aClassDef(modelFqcn)
                .withFieldDefsNotInherited(fieldDefsNotInherited)
                .build()

        val handlerUqcn = Uqcn("${formModelKey}FormHandler")
        val handlerFqcn = Fqcn.valueOf(packageName, handlerUqcn)
        this.handlerClassDef = aClassDef(handlerFqcn).ofType(ClassType.INTERFACE).build()

        val controllerUqcn = Uqcn("${formModelKey}FormController")
        val controllerFqcn = Fqcn.valueOf(packageName, controllerUqcn)
        this.controllerClassDef = aClassDef(controllerFqcn)
                .withClassAnnotation(AnnotationDefs.SPRING_CONTROLLER)
                .build()


    }


    fun findFieldByName(fieldName: String): ClassFieldDef {

        return allFields
                .filter { v -> v.classFieldName.value == fieldName }
                .firstOrNull() ?: throw IllegalArgumentException("No field named " + fieldName + " is defined on form model " + this.formModelKey)

    }


}
