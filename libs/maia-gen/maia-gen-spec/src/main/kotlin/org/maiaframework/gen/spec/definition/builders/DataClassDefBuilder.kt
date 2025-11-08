package org.maiaframework.gen.spec.definition.builders

import org.maiaframework.common.BlankStringException
import org.maiaframework.gen.cache.CacheName
import org.maiaframework.gen.spec.definition.DataClassDef
import org.maiaframework.gen.spec.definition.DataClassKey
import org.maiaframework.gen.spec.definition.DataClassName
import org.maiaframework.gen.spec.definition.EnumDef
import org.maiaframework.gen.spec.definition.flags.WithHandCodedSubclass
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.ParameterizedType
import java.util.LinkedList
import java.util.Optional

class DataClassDefBuilder(
    private val packageName: PackageName,
    private val dataClassName: DataClassName,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?
) {

    private var cacheableDefBuilder: CacheableDefBuilder? = null
    private val dataClassFieldDefBuilders = LinkedList<DataClassFieldDefBuilder>()
    private var dataClassKeyOptional = Optional.empty<DataClassKey>()
    private var withHandcodedSubclass = WithHandCodedSubclass.FALSE


    fun build(): DataClassDef {

        val fieldDefs = buildFieldDefs()

        return DataClassDef.newInstance(
            this.dataClassKeyOptional.orElse(DataClassKey(this.dataClassName.value)),
            this.dataClassName,
            this.packageName,
            fieldDefs,
            this.cacheableDefBuilder?.build(),
            this.withHandcodedSubclass
        )

    }


    private fun buildFieldDefs(): List<DataClassFieldDef> {

        return this.dataClassFieldDefBuilders.map { it.build() }.sorted()

    }


    fun cacheable(init: CacheableDefBuilder.() -> Unit) {

        val builder = CacheableDefBuilder(CacheName(this.dataClassName.value))
        this.cacheableDefBuilder = builder
        builder.init()

    }


    fun dataClassKey(dataClassKey: String): DataClassDefBuilder {

        BlankStringException.throwIfBlank(dataClassKey, "dataClassKey")

        this.dataClassKeyOptional = Optional.of(DataClassKey(dataClassKey))
        return this

    }


    fun field(fieldName: String, fieldType: FieldType): DataClassFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), fieldType)

    }


    fun field(fieldName: String, enumDef: EnumDef): DataClassFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), enumDef)

    }


    private fun newFieldDefBuilder(classFieldName: ClassFieldName, fieldType: FieldType): DataClassFieldDefBuilder {

        return add(
            DataClassFieldDefBuilder(
                classFieldName,
                fieldType,
                this,
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )

    }


//    private fun newFieldDefBuilder(classFieldName: ClassFieldName, simpleTypeDef: SimpleTypeDef): DataClassFieldDefBuilder {
//
//        return add(
//            DataClassFieldDefBuilder(
//                classFieldName,
//                simpleTypeDef,
//                this,
//                this.defaultFieldTypeFieldReaderProvider,
//                this.defaultFieldTypeFieldWriterProvider
//            )
//        )
//
//    }


    private fun newFieldDefBuilder(classFieldName: ClassFieldName, enumDef: EnumDef): DataClassFieldDefBuilder {

        return add(
            DataClassFieldDefBuilder(
                classFieldName,
                enumDef,
                this,
                this.defaultFieldTypeFieldReaderProvider,
                this.defaultFieldTypeFieldWriterProvider
            )
        )


    }


    private fun add(builder: DataClassFieldDefBuilder): DataClassFieldDefBuilder {

        this.dataClassFieldDefBuilders.add(builder)
        return builder

    }


//    fun field(fieldName: String, simpleTypeDef: SimpleTypeDef): DataClassFieldDefBuilder {
//
//        return newFieldDefBuilder(
//            ClassFieldName(fieldName),
//            simpleTypeDef
//        )
//
//    }


    fun field(fieldName: String, listFieldType: ListFieldType): DataClassFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), listFieldType)

    }


    fun field(fieldName: String, mapFieldType: MapFieldType): DataClassFieldDefBuilder {

        return newFieldDefBuilder(ClassFieldName(fieldName), mapFieldType)

    }


    fun withHandcodedSubclass(): DataClassDefBuilder {

        this.withHandcodedSubclass = WithHandCodedSubclass.TRUE
        return this

    }


}
