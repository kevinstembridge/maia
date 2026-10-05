package org.maiaframework.showcase.props

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.ChangeType
import org.maiaframework.props.repo.DatabasePropsRepo
import org.maiaframework.props.repo.PropsRepo
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID


class DatabasePropsRepoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var propsRepo: PropsRepo


    @Test
    fun `the showcase uses the database repo`() {

        assertThat(this.propsRepo).isInstanceOf(DatabasePropsRepo::class.java)

    }


    @Test
    fun `setting an override twice updates the same row and records create then update history`() {

        val name = anyPropertyName()

        this.propsRepo.setPropertyOverride(name, "v1", "alice", "first", null)
        val created = this.propsRepo.getPropertyOrNull(name)!!

        assertThat(created.propertyValue).isEqualTo("v1")
        assertThat(created.version).isEqualTo(1)

        this.propsRepo.setPropertyOverride(name, "v2", "bob", "second", LocalDate.of(2030, 1, 1))
        val updated = this.propsRepo.getPropertyOrNull(name)!!

        assertThat(updated.id).isEqualTo(created.id)
        assertThat(updated.propertyValue).isEqualTo("v2")
        assertThat(updated.version).isEqualTo(2)
        assertThat(updated.lastModifiedByUsername).isEqualTo("bob")
        assertThat(updated.reviewDate).isEqualTo(LocalDate.of(2030, 1, 1))

        val history = this.propsRepo.getPropertyHistory(name).sortedBy { it.version }

        assertThat(history.map { it.version }).containsExactly(1L, 2L)
        assertThat(history.map { it.changeType }).containsExactly(ChangeType.CREATE, ChangeType.UPDATE)
        assertThat(history.map { it.propertyValue }).containsExactly("v1", "v2")
        assertThat(history.map { it.id }.toSet()).containsExactly(created.id)

    }


    @Test
    fun `history is kept separately for each property`() {

        val first = anyPropertyName()
        val second = anyPropertyName()

        this.propsRepo.setPropertyOverride(first, "a", "alice", null, null)
        this.propsRepo.setPropertyOverride(second, "b", "alice", null, null)
        this.propsRepo.setPropertyOverride(second, "c", "alice", null, null)

        assertThat(this.propsRepo.getPropertyHistory(first)).hasSize(1)
        assertThat(this.propsRepo.getPropertyHistory(second)).hasSize(2)

    }


    @Test
    fun `removing an override deletes the row by name`() {

        val name = anyPropertyName()
        val other = anyPropertyName()

        this.propsRepo.setPropertyOverride(name, "v1", "alice", null, null)
        this.propsRepo.setPropertyOverride(other, "v1", "alice", null, null)

        this.propsRepo.removePropertyOverride(name, "alice", "no longer needed")

        val remaining = this.propsRepo.getAllProperties().map { it.propertyName }
        assertThat(remaining).contains(other).doesNotContain(name)

    }


    @Test
    fun `an override can be recreated after it was removed, with a fresh id`() {

        val name = anyPropertyName()

        this.propsRepo.setPropertyOverride(name, "v1", "alice", null, null)
        val firstId = this.propsRepo.getPropertyOrNull(name)!!.id
        this.propsRepo.removePropertyOverride(name, "alice", null)

        this.propsRepo.setPropertyOverride(name, "v2", "alice", null, null)
        val recreated = this.propsRepo.getPropertyOrNull(name)!!

        assertThat(recreated.propertyValue).isEqualTo("v2")
        assertThat(recreated.version).isEqualTo(1)
        assertThat(recreated.id).isNotEqualTo(firstId)

    }


    private fun anyPropertyName() = "test.props.${UUID.randomUUID()}"


}
