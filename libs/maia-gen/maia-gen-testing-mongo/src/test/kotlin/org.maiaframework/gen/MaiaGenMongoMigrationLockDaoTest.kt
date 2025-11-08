package mahana.gen

import org.maiaframework.dao.mongo.migration.MahanaGenMongoMigrationLockDao
import org.maiaframework.dao.mongo.migration.MahanaGenMongoMigrationLockNotAvailableException
import org.maiaframework.dao.mongo.MongoClientFacade
import org.maiaframework.domain.types.CollectionName
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class MaiaGenMongoMigrationLockDaoTest : AbstractIntegrationTest() {


    @Autowired
    private lateinit var dao: MahanaGenMongoMigrationLockDao


    @Autowired
    private lateinit var mongoClientFacade: MongoClientFacade


    @BeforeMethod
    fun beforeMethod() {

        this.mongoClientFacade.deleteMany(CollectionName("maiaGenMigrationLock"), Document())

    }


    @Test
    @Throws(MahanaGenMongoMigrationLockNotAvailableException::class)
    fun testObtainAndReleaseLock() {

        this.dao.obtainLock()

        try {
            this.dao.obtainLock()
            Assert.fail("Expected a MaiaGenMongoMigrationLockNotAvailableException")
        } catch (e: MahanaGenMongoMigrationLockNotAvailableException) {
            //do nothing
        }

        this.dao.releaseLock()

        this.dao.obtainLock()

    }


}
