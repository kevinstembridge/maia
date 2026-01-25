package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.AnnotationDef

object AnnotationDefs {

    val JSON_CREATOR = AnnotationDef(Fqcns.JACKSON_JSON_CREATOR)
    val JSON_INCLUDE = AnnotationDef(Fqcns.JACKSON_JSON_INCLUDE, { "JsonInclude.Include.NON_EMPTY" }, emptyMap())
    val JSON_IGNORE_PROPERTIES = AnnotationDef(Fqcns.JACKSON_JSON_IGNORE_PROPERTIES, attributes = mapOf("ignoreUnknown" to "true"))
    val JSON_PROPERTY = AnnotationDef(Fqcns.JACKSON_JSON_PROPERTY)
    val SPRING_AUTOWIRED = AnnotationDef(Fqcns.SPRING_AUTOWIRED)
    val SPRING_COMPONENT = AnnotationDef(Fqcns.SPRING_COMPONENT)
    val SPRING_CONTROLLER = AnnotationDef(Fqcns.SPRING_CONTROLLER)
    val SPRING_REPOSITORY = AnnotationDef(Fqcns.SPRING_REPOSITORY)
    val SPRING_REST_CONTROLLER = AnnotationDef(Fqcns.SPRING_REST_CONTROLLER)
    val SPRING_SERVICE = AnnotationDef(Fqcns.SPRING_SERVICE)
    val VALIDATION_CONSTRAINT_EMAIL = AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_EMAIL)
    val VALIDATION_CONSTRAINT_ENUM = AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_ENUM)
    val VALIDATION_CONSTRAINT_NOT_BLANK = AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_NOT_BLANK)
    val VALIDATION_CONSTRAINT_NOT_EMPTY = AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_NOT_EMPTY)
    val VALIDATION_CONSTRAINT_NOT_NULL = AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_NOT_NULL)
    val VALIDATION_CONSTRAINT_URL = AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_URL)

}
