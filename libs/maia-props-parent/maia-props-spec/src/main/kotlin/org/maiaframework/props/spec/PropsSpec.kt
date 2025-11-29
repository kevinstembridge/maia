@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.props.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused")
class PropsSpec : AbstractSpec(appKey = AppKey("maia_props"), defaultSchemaName = SchemaName("props")) {


    val propertyEntityDef = entity(
        "org.maiaframework.props", "Props",
        versioned = true,
        recordVersionHistory = true,
        deletable = Deletable.TRUE,
        allowFindAll = AllowFindAll.TRUE,
    ) {
        moduleName("sys_ops")
        tableName(name = "props")
        daoHasSpringAnnotation = false
        field("propertyName", FieldTypes.string) {
            primaryKey()
            lengthConstraint(max = 200)
        }
        field("propertyValue", FieldTypes.string) {
            modifiableBySystem()
            lengthConstraint(max = 2000)
        }
        field_lastModifiedByName()
        field_lastModifiedTimestampUtc()
        field("comment", FieldTypes.string) {
            nullable()
            lengthConstraint(max = 200)
        }
    }


}
