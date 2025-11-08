@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.toggles.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.FieldTypes.mapFieldType

@Suppress("unused")
class TogglesSpec : AbstractSpec(appKey = AppKey("maia_toggles"), defaultSchemaName = SchemaName("toggles")) {


    val contactPersonValueDef = stringValueClass("org.maiaframework.toggles.fields", "ContactPerson")


    val descriptionValueDef = stringValueClass("org.maiaframework.toggles.fields", "Description")


    val featureNameValueDef = stringValueClass("org.maiaframework.toggles", "FeatureName")


    val infoLinkValueDef = stringValueClass("org.maiaframework.toggles.fields", "InfoLink")


    val ticketKeyValueDef = stringValueClass("org.maiaframework.toggles.fields", "TicketKey")


    val activationStrategyParameterDef = dataClass(
        "org.maiaframework.toggles.activation",
        "ActivationStrategyParameter"
    ) {
        cacheable {}
        field("name", FieldTypes.string)
        field("value", FieldTypes.string)
    }


    val activationStrategyDescriptorDef = dataClass(
        "org.maiaframework.toggles.activation",
        "ActivationStrategyDescriptor"
    ) {
        cacheable {}
        field("id", FieldTypes.string)
        field("parameter", fieldListOf(activationStrategyParameterDef))
    }


    val featureToggleEntityDef = entity(
        "org.maiaframework.toggles", "FeatureToggle",
        versioned = true,
        recordVersionHistory = true,
        deletable = Deletable.TRUE,
        allowFindAll = AllowFindAll.TRUE,
    ) {
        moduleName("sys_ops")
        cacheable {

        }
        daoHasSpringAnnotation = false
        field("featureName", featureNameValueDef) {
            primaryKey()
            lengthConstraint(max = 200)
        }
        field("enabled", FieldTypes.boolean) {
            modifiableBySystem()
        }
        field("description", descriptionValueDef) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 500)
        }
        field("ticketKey", ticketKeyValueDef) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 100)
        }
        field("reviewDate", FieldTypes.localDate) {
            nullable()
            modifiableBySystem()
        }
        field("contactPerson", contactPersonValueDef) {
            nullable()
            lengthConstraint(max = 100)
            modifiableBySystem()
        }
        field("infoLink", infoLinkValueDef) {
            nullable()
            lengthConstraint(max = 300)
            modifiableBySystem()
        }
        field("attributes", mapFieldType(FieldTypes.string, FieldTypes.string))
        field("lastModifiedBy", FieldTypes.string) {
            modifiableBySystem()
            lengthConstraint(max = 100)
        }
        field_lastModifiedTimestampUtc()
        field("comment", FieldTypes.string) {
            nullable()
            lengthConstraint(max = 200)
        }
        field("activationStrategies", fieldListOf(activationStrategyDescriptorDef))
    }


    val featureToggleResponseDtoDef = simpleResponseDto(
        "org.maiaframework.toggles",
        "FeatureToggle"
    ) {
        field("featureName", featureNameValueDef)
    }


    val userDetailsHzDef = hazelcastDtoDef("org.maiaframework.toggles", "FeatureState") {
        field("featureName", featureNameValueDef)
        field("enabled", FieldTypes.boolean)
    }


}
