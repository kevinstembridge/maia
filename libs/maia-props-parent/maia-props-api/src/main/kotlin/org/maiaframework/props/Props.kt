package org.maiaframework.props

import org.maiaframework.lang.text.StringFunctions
import org.maiaframework.props.repo.PropsRepo
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles


class Props(private val propsRepo: PropsRepo, private val env: Environment) {


    fun getStringOrNull(propertyName: String): String? {

        val propertyValue = this.propsRepo.getPropertyOrNull(propertyName)
                ?.propertyValue
                ?: this.env.getProperty(propertyName)

        return StringFunctions.stripToNull(propertyValue)

    }


    fun getString(propertyName: String): String {

        return getStringOrNull(propertyName)
                ?: throw RuntimeException("No property found with name '$propertyName'")

    }


    fun getIntOrNull(propertyName: String): Int? {

        val rawValue = getStringOrNull(propertyName)

        try {
            return rawValue?.toInt()
        } catch (_: NumberFormatException) {
            throw IllegalStateException("Found an invalid number '$rawValue' under property name '$propertyName'")
        }

    }


    fun getLongOrNull(propertyName: String): Long? {

        val rawValue = getStringOrNull(propertyName)

        try {
            return rawValue?.toLong()
        } catch (_: NumberFormatException) {
            throw IllegalStateException("Found an invalid number '$rawValue' under property name '$propertyName'")
        }

    }


    fun isSpringProfileActive(profileName: String): Boolean {

        return this.env.acceptsProfiles(Profiles.of(profileName))

    }


}
