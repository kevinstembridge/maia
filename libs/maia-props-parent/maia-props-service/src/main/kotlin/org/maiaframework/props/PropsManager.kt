package org.maiaframework.props

import org.maiaframework.props.repo.PropsRepo

class PropsManager(private val propsRepo: PropsRepo) {


    fun setProperty(
            propertyName: String,
            propertyValue: String,
            username: String,
            comment: String?
    ) {

        this.propsRepo.setPropertyOverride(
                propertyName,
                propertyValue,
                username,
                comment
        )

    }


    fun removeProperty(propertyName: String, username: String, comment: String?) {

        this.propsRepo.removePropertyOverride(
                propertyName,
                username,
                comment
        )

    }


}
