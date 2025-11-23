@file:Suppress("MemberVisibilityCanBePrivate")

package org.maiaframework.toggles.spec


import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModuleName
import org.maiaframework.gen.spec.definition.flags.AllowFindAll
import org.maiaframework.gen.spec.definition.flags.Deletable
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.FieldTypes.mapFieldType

@Suppress("unused")
class TogglesSpec : AbstractSpec(appKey = AppKey("maia_toggles"), defaultSchemaName = SchemaName("toggles")) {


    val moduleName = ModuleName.of("maia_toggles")


    val contactPersonValueDef = stringValueClass("org.maiaframework.toggles.fields", "ContactPerson")


    val descriptionValueDef = stringValueClass("org.maiaframework.toggles.fields", "Description")


    val featureNameValueDef = stringValueClass("org.maiaframework.toggles", "FeatureName")


    val infoLinkValueDef = stringValueClass("org.maiaframework.toggles.fields", "InfoLink")


    val ticketKeyValueDef = stringValueClass("org.maiaframework.toggles.fields", "TicketKey")


    val activationStrategyParameterDef = dataClass("org.maiaframework.toggles.activation", "ActivationStrategyParameter") {
        cacheable {}
        field("name", FieldTypes.string)
        field("value", FieldTypes.string)
    }


    val activationStrategyDescriptorDef = dataClass("org.maiaframework.toggles.activation", "ActivationStrategyDescriptor") {
        cacheable {}
        field("id", FieldTypes.string)
        field("parameters", fieldListOf(activationStrategyParameterDef))
    }


    val featureToggleEntityDef = entity(
        "org.maiaframework.toggles", "FeatureToggle",
        versioned = true,
        recordVersionHistory = true,
        deletable = Deletable.TRUE,
        allowFindAll = AllowFindAll.TRUE,
    ) {
        moduleName("sys_ops")
        cacheable { }
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
            modifiableBySystem()
            lengthConstraint(max = 100)
        }
        field("infoLink", infoLinkValueDef) {
            nullable()
            modifiableBySystem()
            lengthConstraint(max = 300)
        }
        field("attributes", mapFieldType(FieldTypes.string, FieldTypes.string)) {
            nullable()
            modifiableBySystem()
        }
        field("lastModifiedBy", FieldTypes.string) {
            modifiableBySystem()
            lengthConstraint(max = 100)
        }
        field_lastModifiedTimestampUtc()
        field("comment", FieldTypes.string) {
            nullable()
            lengthConstraint(max = 200)
            modifiableBySystem()
        }
        field("activationStrategies", fieldListOf(activationStrategyDescriptorDef)) {
//            modifiableBySystem()
        }
    }


    val featureToggleResponseDtoDef = simpleResponseDto("org.maiaframework.toggles", "FeatureToggle") {
        field("featureName", featureNameValueDef)
        field("enabled", FieldTypes.boolean)
        field("description", descriptionValueDef) { nullable() }
        field("ticketKey", ticketKeyValueDef ) { nullable() }
        field("reviewDate", FieldTypes.localDate ) { nullable() }
        field("contactPerson", contactPersonValueDef ) { nullable() }
        field("infoLink", infoLinkValueDef ) { nullable() }
        field("attributes", mapFieldType(FieldTypes.string, FieldTypes.string) ) { nullable() }
        field("comment", FieldTypes.string) { nullable() }
        field("activationStrategies", fieldListOf(activationStrategyDescriptorDef))
        field("lastModifiedBy", FieldTypes.string)
        field("lastModifiedTimestampUtc", FieldTypes.instant)
        field("createdTimestampUtc", FieldTypes.instant)
    }


    val featureToggleIsActiveResponseDtoDef = simpleResponseDto("org.maiaframework.toggles", "FeatureToggleIsActive") {
        field("active", FieldTypes.boolean)
    }


    val setFeatureToggleRequestDtoDef = requestDto(
        "org.maiaframework.toggles",
        "SetFeatureToggle",
        moduleName = moduleName
    ) {
        field("featureName", featureNameValueDef)
        field("enabled", FieldTypes.boolean)
        field("comment", FieldTypes.string) { nullable() }
        field("version", FieldTypes.long)
    }


}
