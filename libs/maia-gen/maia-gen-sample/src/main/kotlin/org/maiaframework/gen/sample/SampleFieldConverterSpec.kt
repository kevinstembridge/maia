package org.maiaframework.gen.sample

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.DatabaseType
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.Fqcn

@Suppress("unused")
class SampleFieldConverterSpec : AbstractSpec(appKey = AppKey("field_converter"), defaultSchemaName = SchemaName("field_converter"), DatabaseType.MONGO) {

    init {

        defaultFieldReader(FieldTypes.string, Fqcn.valueOf("org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldTypeLevelFieldReader"))
        defaultFieldWriter(FieldTypes.string, Fqcn.valueOf("org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldTypeLevelFieldWriter"))

        entity("org.maiaframework.gen.sample.fieldconverters", "FieldConversion") {
            field("someStringWithFieldTypeLevelReader", FieldTypes.string) {
                tableColumnName("ft")
                lengthConstraint(max = 100)
            }
            field("someStringWithFieldLevelReader", FieldTypes.string) {
                lengthConstraint(max = 100)
                tableColumnName("fl")
                fieldReader(Fqcn.valueOf("org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldLevelFieldReader"))
                fieldWriter(Fqcn.valueOf("org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldLevelFieldWriter"))
            }
        }

    }


}
