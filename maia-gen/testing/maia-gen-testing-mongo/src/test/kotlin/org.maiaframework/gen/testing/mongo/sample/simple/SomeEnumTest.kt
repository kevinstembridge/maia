package org.maiaframework.gen.testing.mongo.sample.simple

import org.maiaframework.gen.testing.mongo.sample.simple.SomeEnum
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class SomeEnumTest {


    @Test
    fun testEnumValues() {

        val actualValues = SomeEnum.values()
        val expectedValues = arrayOf(SomeEnum.OK, SomeEnum.NOT_OK)

        assertEquals(actualValues, expectedValues)

    }


}
