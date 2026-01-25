package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.persist.BsonCompatibleType
import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.TypescriptCompatibleType
import org.maiaframework.gen.spec.definition.TypescriptCompatibleTypes
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.jdbc.JdbcCompatibleType


class ClassDef(
    val nonPrimitiveType: NonPrimitiveType,
    val fqcn: Fqcn,
    val isAbstract: Boolean,
    val classType: ClassType,
    val classVisibility: ClassVisibility,
    val fieldDefsNotInherited: List<ClassFieldDef>,
    val classAnnotations: List<AnnotationDef>,
    val constructorAnnotations: List<AnnotationDef>,
    val interfacesImplemented: List<ParameterizedType>,
    val superclassDef: ClassDef?
) {


    val packageName = fqcn.packageName


    val uqcn = fqcn.uqcn


    val fieldsInherited: List<ClassFieldDef>
        get() = this.superclassDef?.allFields ?: emptyList()


    val fieldsInheritedSorted: List<ClassFieldDef>
        get() = this.superclassDef?.allFieldsSorted ?: emptyList()


    val allFields: List<ClassFieldDef>
        get() = listOf(this.fieldDefsNotInherited, this.fieldsInherited).flatten()


    val allFieldsSorted: List<ClassFieldDef>
        get() = listOf(this.fieldDefsNotInherited, this.fieldsInheritedSorted).flatten().sorted()


    val hasSuperType: Boolean = this.superclassDef != null || this.interfacesImplemented.isNotEmpty()


    val isModifiable = allFieldsSorted.any { it.isModifiableBySystem || it.isEditableByUser.value }


    val typescriptDtoImport = TypescriptImport(uqcn.value, "@app/gen-components/${this.packageName.asTypescriptDirs()}/$uqcn")


    val typescriptDtoImportStatement = "import {$uqcn} from '@app/gen-components/${this.packageName.asTypescriptDirs()}/$uqcn';"


    val typescriptRenderedFilePath = "app/gen-components/${this.packageName.asTypescriptDirs()}/${uqcn}.ts"


    val fieldType = FqcnFieldType(
        this.fqcn,
        bsonCompatibleType = BsonCompatibleType.DOCUMENT,
        typescriptCompatibleType = TypescriptCompatibleTypes.any,
        jdbcCompatibleType = JdbcCompatibleType.jsonb,
        sqlType = "jsonb",
        elasticMappingType = null,
        hazelcastCompatibleType = null
    )


    private val rowMapperFqcn = this.fqcn.withSuffix("RowMapper")


    val rowMapperClassDef: ClassDef
        get() = aClassDef(this.rowMapperFqcn)
            .withInterface(ParameterizedType(Fqcns.MAIA_JDBC_ROW_MAPPER, ParameterizedType(this.fqcn)))
            .build()


    fun isFieldFromSuperclass(classFieldDef: ClassFieldDef): Boolean {

        return fieldsInheritedSorted
            .any { fieldDef -> fieldDef.classFieldName == classFieldDef.classFieldName }

    }


    fun withUqcnPrefix(prefix: String): ClassDef {

        return ClassDef(
            this.nonPrimitiveType.withPrefix(prefix),
            this.fqcn.withPrefix(prefix),
            this.isAbstract,
            this.classType,
            this.classVisibility,
            this.fieldDefsNotInherited,
            this.classAnnotations,
            this.constructorAnnotations,
            this.interfacesImplemented,
            this.superclassDef
        )

    }


    fun withAbstract(isAbstract: Boolean): ClassDef {

        return ClassDef(
            this.nonPrimitiveType,
            this.fqcn,
            isAbstract,
            this.classType,
            this.classVisibility,
            this.fieldDefsNotInherited,
            this.classAnnotations,
            this.constructorAnnotations,
            this.interfacesImplemented,
            this.superclassDef
        )

    }


    fun findFieldByPath(fieldPath: String): ClassFieldDef {

        // TODO if fieldPath has components, find the

        val pathElements = fieldPath.split(".")

        val firstFieldName = pathElements.first()


        val classFieldDef = (this.allFieldsSorted.find { it.classFieldName.value == firstFieldName }
            ?: throw IllegalStateException("No field found with name '$firstFieldName' on classDef $fqcn"))

        return if (pathElements.size > 1) {
            classFieldDef.findEmbeddedField(pathElements.drop(1))
        } else {
            classFieldDef
        }

    }


}
