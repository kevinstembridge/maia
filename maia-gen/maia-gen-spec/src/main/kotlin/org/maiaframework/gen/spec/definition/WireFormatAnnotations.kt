package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.BigDecimalFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef


/** BigDecimal values travel as JSON strings so that no precision is lost in transit (see ADR-0006). */
fun ClassFieldDef.withWireFormatAnnotations(): ClassFieldDef {

    if (this.fieldType !is BigDecimalFieldType) {
        return this
    }

    return this.copy(annotationDefs = (this.annotationDefs + AnnotationDefs.JSON_FORMAT_STRING).toSortedSet())

}
