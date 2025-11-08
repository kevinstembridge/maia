package mahana.gen.testing.mongo.sample.fieldconverters

import org.maiaframework.gen.AbstractIntegrationTest
import org.maiaframework.domain.DomainId
import org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldLevelFieldReader
import org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldLevelFieldWriter
import org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldTypeLevelFieldReader
import org.maiaframework.gen.sample.fieldconverters.FieldConverterTestFieldTypeLevelFieldWriter
import org.springframework.beans.factory.annotation.Autowired
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.time.Instant
import java.util.*

class FieldConverterTest : AbstractIntegrationTest() {


//    @Autowired
//    private lateinit var fieldConversionDao: FieldConversionDao

    @Autowired
    private lateinit var fieldConverterTestFieldTypeLevelFieldReader: FieldConverterTestFieldTypeLevelFieldReader

    @Autowired
    private lateinit var fieldConverterTestFieldLevelFieldReader: FieldConverterTestFieldLevelFieldReader

    @Autowired
    private lateinit var fieldConverterTestFieldTypeLevelFieldWriter: FieldConverterTestFieldTypeLevelFieldWriter

    @Autowired
    private lateinit var fieldConverterTestFieldLevelFieldWriter: FieldConverterTestFieldLevelFieldWriter


    @Test
    fun shouldUseFieldTypeConverter() {

        val someStringWithFieldLevelConverters = UUID.randomUUID().toString()
        val someStringWithFieldTypeLevelConverters = UUID.randomUUID().toString()

        val expectedFieldTypeValueAppendedByWriter = UUID.randomUUID().toString()
        val expectedFieldTypeValueAppendedByReader = UUID.randomUUID().toString()
        val expectedFieldValueAppendedByWriter = UUID.randomUUID().toString()
        val expectedFieldValueAppendedByReader = UUID.randomUUID().toString()

        this.fieldConverterTestFieldTypeLevelFieldWriter.setNextValue(expectedFieldTypeValueAppendedByWriter)
        this.fieldConverterTestFieldLevelFieldWriter.setNextValue(expectedFieldValueAppendedByWriter)

        this.fieldConverterTestFieldTypeLevelFieldReader.setNextValue(expectedFieldTypeValueAppendedByReader)
        this.fieldConverterTestFieldLevelFieldReader.setNextValue(expectedFieldValueAppendedByReader)

//        val entity = FieldConversionEntity(
//                Instant.now(),
//                DomainId.newId(),
//                someStringWithFieldLevelConverters,
//                someStringWithFieldTypeLevelConverters)
//
//        this.fieldConversionDao.insert(entity)
//
//        val foundEntity = this.fieldConversionDao.findById(entity.id)
//
//        assertEquals(foundEntity.someStringWithFieldTypeLevelReader, someStringWithFieldTypeLevelConverters + expectedFieldTypeValueAppendedByWriter + expectedFieldTypeValueAppendedByReader)
//        assertEquals(foundEntity.someStringWithFieldLevelReader, someStringWithFieldLevelConverters + expectedFieldValueAppendedByWriter + expectedFieldValueAppendedByReader)

    }


}
