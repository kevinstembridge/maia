package org.maiaframework.showcase.party

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.LifecycleState
import org.maiaframework.showcase.org.OrganizationEntity
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyDomainId
import org.maiaframework.testing.domain.Anys.anyInstant
import org.maiaframework.testing.domain.Anys.anyOrgName
import java.time.Instant


data class OrgEntityTestBuilder(
    val createdById: DomainId = Anys.defaultCreatedById,
    val createdTimestamp: Instant = anyInstant(),
    val id: DomainId = anyDomainId(),
    val lastModifiedById: DomainId = createdById,
    val lastModifiedTimestamp: Instant = createdTimestamp,
    val lifecycleState: LifecycleState = LifecycleState.ACTIVE,
    val name: String = anyOrgName(),
    val version: Long = 1L
) {


    fun build(): OrganizationEntity {

        val displayName = name

        return OrganizationEntity(
            createdById,
            createdTimestamp,
            displayName,
            id,
            lastModifiedById,
            lastModifiedTimestamp,
            lifecycleState,
            name,
            version
        )

    }

}
