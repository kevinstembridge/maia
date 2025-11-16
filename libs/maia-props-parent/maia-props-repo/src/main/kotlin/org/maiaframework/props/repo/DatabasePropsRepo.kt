package org.maiaframework.props.repo

import org.maiaframework.props.*
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import java.util.*


class DatabasePropsRepo(
    private val propsDao: PropsDao,
    private val propsHistoryDao: PropsHistoryDao
) : PropsRepo {

    private var properties: SortedMap<String, PropsEntity> = TreeMap()


    @EventListener
    fun handleContextRefreshEvent(event: ContextRefreshedEvent) {
        refresh()
    }


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

        this.propsDao.upsertByPropertyName(propsEntity)
        refresh()

    }


    override fun removePropertyOverride(
        propertyName: String,
        username: String,
        comment: String?
    ) {

        this.propsDao.deleteByPrimaryKey(propertyName)

    }


    override fun refresh() {

        this.properties = this.propsDao.findAll()
            .associateBy { it.propertyName }
            .toSortedMap()

    }


    override fun getPropertyHistory(propertyName: String): List<PropsHistoryEntity> {

        val filter = PropsHistoryEntityFilters().propertyName eq propertyName
        return this.propsHistoryDao.findAllBy(filter)

    }


    override fun getAllProperties(): List<PropsEntity> {

        return this.propsDao.findAll()

    }


}
