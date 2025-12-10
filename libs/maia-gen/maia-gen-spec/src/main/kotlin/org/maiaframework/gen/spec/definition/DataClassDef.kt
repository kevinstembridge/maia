package org.maiaframework.gen.spec.definition


import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.flags.WithHandCodedSubclass
import org.maiaframework.gen.spec.definition.lang.*
import java.util.*


class DataClassDef private constructor(
    val dataClassKey: DataClassKey,
    dataClassName: DataClassName,
    packageName: PackageName,
    fieldDefs: List<DataClassFieldDef>,
    val cacheableDef: CacheableDef?,
    private val withHandcodedSubclass: WithHandCodedSubclass
) {


    val classDef: ClassDef


    val fields: List<DataClassFieldDef> = fieldDefs.sorted()


    val fqcn: Fqcn
        get() = this.classDef.fqcn


    val uqcn: Uqcn
        get() = this.classDef.uqcn


    val isWithHandcodedSubclass: Boolean = this.withHandcodedSubclass.value


    val classFieldDefs: List<ClassFieldDef>
        get() = this.fields.map { it.classFieldDef }


    val hazelcastSerializerClassDef by lazy {
        aClassDef(packageName.uqcn(dataClassName.withSuffix("Serializer").value))
        .withClassAnnotation(AnnotationDefs.SPRING_COMPONENT)
        .withInterface(ParameterizedTypes.hazelcastCompactSerializer(ParameterizedType(fqcn)))
        .build()
    }


    init {

        val fqcn = packageName.uqcn(dataClassName.value)

        this.classDef = aClassDef(fqcn)
            .ofType(ClassType.DATA_CLASS)
            .withFieldDefsNotInherited(fieldDefs.map { it.classFieldDef })
            .build()

    }


    override fun equals(o: Any?): Boolean {

        if (this === o) {
            return true
        }

        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val entityDef = o as DataClassDef?
        return dataClassKey == entityDef!!.dataClassKey

    }


    override fun hashCode(): Int {

        return Objects.hash(dataClassKey)

    }


    override fun toString(): String {

        return "DataClassDef{" + this.dataClassKey + "}"

    }

    companion object {


        fun newInstance(
            dataClassKey: DataClassKey,
            dataClassName: DataClassName,
            packageName: PackageName,
            classFieldDefs: List<DataClassFieldDef>,
            cacheableDef: CacheableDef?,
            withHandcodedSubclass: WithHandCodedSubclass
        ): DataClassDef {

            return DataClassDef(
                dataClassKey,
                dataClassName,
                packageName,
                classFieldDefs,
                cacheableDef,
                withHandcodedSubclass
            )

        }
    }


}
