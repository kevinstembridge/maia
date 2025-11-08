package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.gen.spec.definition.lang.*


class ClassDefBuilder private constructor(private val nonPrimitiveType: NonPrimitiveType) {


    private var classType = ClassType.CLASS


    private val classVisibility = ClassVisibility.PUBLIC


    private val fieldDefsNotInherited = mutableListOf<ClassFieldDef>()


    private val classAnnotations = mutableListOf<AnnotationDef>()


    private val constructorAnnotations = mutableListOf<AnnotationDef>()


    private val interfacesImplemented = mutableListOf<ParameterizedType>()


    private var superclassDefOptional: ClassDef? = null


    private var isAbstract: Boolean = false


    fun build(): ClassDef {

        return ClassDef(
            this.nonPrimitiveType,
            this.nonPrimitiveType.fqcn,
            this.isAbstract,
            this.classType,
            this.classVisibility,
            this.fieldDefsNotInherited.toList(),
            this.classAnnotations.toList(),
            this.constructorAnnotations.toList(),
            this.interfacesImplemented.toList(),
            this.superclassDefOptional
        )

    }


    fun withFieldDefsNotInherited(fieldDefsNotInherited: List<ClassFieldDef>): ClassDefBuilder {

        this.fieldDefsNotInherited.addAll(fieldDefsNotInherited)
        return this

    }


    fun withSuperclass(superclassDef: ClassDef?): ClassDefBuilder {

        this.superclassDefOptional = superclassDef
        return this

    }


    fun withInterface(interfaceImplemented: ParameterizedType): ClassDefBuilder {

        this.interfacesImplemented.add(interfaceImplemented)
        return this

    }


    fun withInterfaces(vararg interfacesImplemented: ParameterizedType): ClassDefBuilder {

        this.interfacesImplemented.addAll(interfacesImplemented)
        return this

    }


    fun ofType(classType: ClassType): ClassDefBuilder {

        this.classType = classType
        return this

    }


    fun withClassAnnotation(vararg annotationDefs: AnnotationDef): ClassDefBuilder {

        this.classAnnotations.addAll(annotationDefs)
        return this

    }


    fun withConstructorAnnotation(annotationDef: AnnotationDef): ClassDefBuilder {

        this.constructorAnnotations.add(annotationDef)
        return this

    }


    fun withAbstract(isAbstract: Boolean): ClassDefBuilder {

        this.isAbstract = isAbstract
        return this

    }


    companion object {


        fun aClassDef(fqcn: Fqcn): ClassDefBuilder {

            return ClassDefBuilder(ParameterizedType(fqcn))

        }


        fun aClassDef(nonPrimitiveType: NonPrimitiveType): ClassDefBuilder {

            return ClassDefBuilder(nonPrimitiveType)

        }


    }


}
