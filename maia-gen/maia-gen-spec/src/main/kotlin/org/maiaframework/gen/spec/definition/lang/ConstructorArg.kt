package org.maiaframework.gen.spec.definition.lang

class ConstructorArg(
    val classFieldDef: ClassFieldDef,
    annotationDefs: List<AnnotationDef> = emptyList(),
    renderValidationAnnotations: Boolean = false
) : Comparable<ConstructorArg> {

    private val validationAnnotations = if (renderValidationAnnotations) classFieldDef.validationAnnotations else emptyList()

    val annotationDefs = annotationDefs.plus(classFieldDef.annotationDefs).plus(validationAnnotations).toSortedSet()

    override fun compareTo(other: ConstructorArg): Int {

        return this.classFieldDef.compareTo(other.classFieldDef)

    }


}
