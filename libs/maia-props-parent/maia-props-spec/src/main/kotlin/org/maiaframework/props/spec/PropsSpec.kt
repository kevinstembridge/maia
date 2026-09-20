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


    val readAuthority = authority("MAIA_PROPS_READ")


    val writeAuthority = authority("MAIA_PROPS_WRITE")


    val changeTypeEnumDef = enumDef("org.maiaframework.domain.ChangeType") {
        provided()
    }


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
        field_lastModifiedByUsername()
        field_lastModifiedTimestamp()
        field("comment", FieldTypes.string) {
            nullable()
            lengthConstraint(max = 200)
        }
    }


    val propertyDtoDef = simpleResponseDto("org.maiaframework.props", "Property") {
        field("propertyName", FieldTypes.string)
        field("effectiveValue", FieldTypes.string) {
            nullable()
        }
        field("isOverridden", FieldTypes.boolean)
        field("environmentValue", FieldTypes.string) {
            nullable()
        }
        field("sourceName", FieldTypes.string) {
            nullable()
        }
        field("lastModifiedByUsername", FieldTypes.string) {
            nullable()
        }
        field("lastModifiedTimestamp", FieldTypes.instant) {
            nullable()
        }
        field("comment", FieldTypes.string) {
            nullable()
        }
    }


    val propertyHistoryItemDtoDef = simpleResponseDto("org.maiaframework.props", "PropertyHistoryItem") {
        field("propertyName", FieldTypes.string)
        field("propertyValue", FieldTypes.string)
        field("changeType", changeTypeEnumDef)
        field("lastModifiedByUsername", FieldTypes.string)
        field("lastModifiedTimestamp", FieldTypes.instant)
        field("comment", FieldTypes.string) {
            nullable()
        }
        field("version", FieldTypes.long)
    }


}
