package org.maiaframework.dao.mongo.migration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.util.*
import java.util.regex.Pattern

@Component
class MaiaGenMongoMigrator(
    private val maiaGenMongoMigrationLockDao: MaiaGenMongoMigrationLockDao,
    private val maiaGenMongoMigrationEntryDao: MaiaGenMongoMigrationEntryDao,
    private val migrations: List<MaiaGenMongoMigration>
): InitializingBean {


    override fun afterPropertiesSet() {

        runMigrations()

    }


    fun runMigrations() {

        val migrationsById = mapToMigrationId(migrations)

        try {
            obtainLock()
        } catch (e: MaiaGenMongoMigrationLockNotAvailableException) {
            LOGGER.info(e.message)
            return
        }

        try {

            migrationsById.entries.forEach { this.runMigration(it) }

        } finally {
            releaseLock()
        }

    }


    private fun mapToMigrationId(migrations: Collection<MaiaGenMongoMigration>): SortedMap<MaiaGenMigrationId, MaiaGenMongoMigration> {

        return migrations.groupBy { this.getMigrationId(it) }.mapValues { it.value.first() }.toSortedMap()

    }


    private fun getMigrationId(maiaGenMongoMigration: MaiaGenMongoMigration): MaiaGenMigrationId {

        val simpleClassName = maiaGenMongoMigration.javaClass.simpleName

        val matcher = MIGRATION_ID_PATTERN.matcher(simpleClassName)

        if (matcher.matches() == false) {
            throw RuntimeException("Invalid MaiaGenMongoMigration class name: " + maiaGenMongoMigration.javaClass.canonicalName)
        }

        val idString = matcher.group(1)

        try {
            return MaiaGenMigrationId(Integer.parseInt(idString))
        } catch (e: NumberFormatException) {
            throw RuntimeException("Invalid MaiaGenMongoMigration class name: " + maiaGenMongoMigration.javaClass.canonicalName)
        }

    }


    @Throws(MaiaGenMongoMigrationLockNotAvailableException::class)
    private fun obtainLock() {

        this.maiaGenMongoMigrationLockDao.obtainLock()
        LOGGER.info("MaiaGen migration lock obtained.")

    }

    private fun runMigration(migrationEntry: Map.Entry<MaiaGenMigrationId, MaiaGenMongoMigration>) {

        val migrationId = migrationEntry.key

        if (migrationShouldBeRun(migrationId)) {
            val migration = migrationEntry.value
            LOGGER.info("Applying MaiaGen Migration: {} - {}", migrationId, migration.changeDescription)
            migration.applyMigration()
            recordMigrationEntry(migrationId, migration)
        }

    }


    private fun migrationShouldBeRun(migrationId: MaiaGenMigrationId): Boolean {

        val result = this.maiaGenMongoMigrationEntryDao.notExistsById(migrationId)
        LOGGER.info("Should migration {} be run = {}", migrationId, result)
        return result

    }


    private fun recordMigrationEntry(migrationId: MaiaGenMigrationId, migration: MaiaGenMongoMigration) {

        this.maiaGenMongoMigrationEntryDao.insert(migrationId, migration)

    }


    private fun releaseLock() {

        this.maiaGenMongoMigrationLockDao.releaseLock()
        LOGGER.info("MaiaGen migration lock released.")

    }


    companion object {

        private val LOGGER = LoggerFactory.getLogger(MaiaGenMongoMigrator::class.java)
        private val MIGRATION_ID_PATTERN = Pattern.compile("^Migration_(\\d*)_.*")

    }


}
