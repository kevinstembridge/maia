package org.maiaframework.props.repo

import org.maiaframework.domain.DomainId
import org.maiaframework.props.PropsEntity
import org.maiaframework.props.PropsHistoryEntity
import java.time.Instant

class InMemoryPropsRepo: PropsRepo {

    private val properties = sortedMapOf<String, PropsEntity>()


    override fun getPropertyOrNull(propertyName: String): PropsEntity? {

        return this.properties[propertyName]

    }


    override fun setPropertyOverride(
        propertyName: String,
        propertyValue: String,
        modifiedBy: String,
        comment: String?
    ) {

        val propsEntity = PropsEntity.newInstance(
                comment,
                modifiedBy,
                propertyName,
                propertyValue
        )

        this.properties[propertyName] = propsEntity

    }


    override fun removePropertyOverride(
        propertyName: String,
        username: String,
        comment: String?
    ) {

        this.properties.remove(propertyName)

    }


    override fun refresh() {

        this.properties.clear()

    }


    override fun getPropertyHistory(propertyName: String): List<PropsHistoryEntity> {

        return emptyList()

    }


    override fun getAllProperties(): List<PropsEntity> {

        return this.properties.values.toList()

    }


}
