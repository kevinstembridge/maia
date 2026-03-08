package org.maiaframework.showcase.testing.fixtures

import org.maiaframework.common.logging.getLogger
import org.maiaframework.domain.auth.EncryptedPassword
import org.maiaframework.jdbc.JdbcOps
import org.maiaframework.jdbc.SchemaAndTableName
import org.maiaframework.showcase.party.UserEntityTestBuilder
import org.maiaframework.showcase.testing.MaiaShowcaseAnys
import org.maiaframework.showcase.user.UserDao
import org.maiaframework.showcase.user.UserEntityMeta
import org.maiaframework.testing.domain.Anys
import org.maiaframework.testing.domain.Anys.anyPassword
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class Fixtures(
    private val userDao: UserDao,
    private val passwordEncoder: PasswordEncoder,
    private val jdbcOps: JdbcOps
) {


    private val logger = getLogger<Fixtures>()


    private val uniqueEmailAddressCounter = AtomicInteger(1)


    private val userFixtures = mutableListOf<UserFixture>()


    fun aUser(
        vararg userFixtureConfigurers: (UserEntityTestBuilder) -> UserEntityTestBuilder,
    ): UserFixture {

        val rawPassword = anyPassword()
        val encryptedPassword = EncryptedPassword(this.passwordEncoder.encode(rawPassword)!!)
        val initialUserEntityTestBuilder = UserEntityTestBuilder(encryptedPassword = encryptedPassword)

        val updatedUserEntityTestBuilder = userFixtureConfigurers.fold(initialUserEntityTestBuilder) { carry, fn -> fn.invoke(carry) }

        val userEntity = updatedUserEntityTestBuilder.build()

        val userFixture = UserFixture(userEntity, rawPassword)

        this.userFixtures.add(userFixture)

        return userFixture

    }


    fun resetDatabaseState() {

        logger.info("Resetting database state")

        deleteParties()

        try {
            this.userDao.insert(MaiaShowcaseAnys.defaultUser)
        } catch (_: DuplicateKeyException) {
            //ignore
        }

        if (this.userFixtures.isNotEmpty()) {
            logger.info("Inserting ${this.userFixtures.size} user fixtures")
            this.userFixtures.forEach { userDao.insert(it.userEntity) }
        }

    }


    private fun truncateTable(tableName: SchemaAndTableName, cascade: Boolean = true) {

        this.jdbcOps.update("truncate $tableName${if (cascade) " cascade" else ""}")

    }


    private fun deleteParties() {

        this.jdbcOps.update("delete from ${UserEntityMeta.SCHEMA_AND_TABLE_NAME} where id != '${Anys.defaultCreatedById}'")

    }


}
