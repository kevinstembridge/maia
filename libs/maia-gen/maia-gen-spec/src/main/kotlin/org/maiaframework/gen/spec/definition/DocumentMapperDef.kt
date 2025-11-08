package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassDef
import org.maiaframework.gen.spec.definition.lang.DocumentMapperFieldDef
import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.jdbc.TableName

class DocumentMapperDef(
    val dtoFqcn: Fqcn,
    val tableName: TableName,
    val fieldDefs: List<DocumentMapperFieldDef>
) {


    val classDef: ClassDef = ClassDefBuilder.aClassDef(dtoFqcn.withSuffix("DocumentMapper"))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .build()


}
