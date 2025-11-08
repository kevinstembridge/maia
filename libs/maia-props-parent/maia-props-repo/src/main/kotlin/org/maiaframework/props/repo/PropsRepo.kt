package org.maiaframework.props.repo

import org.maiaframework.props.PropsEntity
import org.maiaframework.props.PropsHistoryEntity


interface PropsRepo {

    fun getPropertyOrNull(propertyName: String): PropsEntity?

    fun setPropertyOverride(propertyName: String, propertyValue: String, modifiedBy: String, comment: String?)

    fun removePropertyOverride(propertyName: String, username: String, comment: String?)

    fun refresh()

    fun getPropertyHistory(propertyName: String): List<PropsHistoryEntity>

    fun getAllProperties(): List<PropsEntity>

}
