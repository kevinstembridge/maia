package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.mongo.CollectionFieldName
import org.testng.Assert.assertEquals
import org.testng.annotations.Test


class CollectionFieldNameTest {


    @Test
    fun testToValidJavaIdentifier() {

        assertEquals(CollectionFieldName("abc").toValidJavaIdentifier(), "abc")
        assertEquals(CollectionFieldName("a-bc").toValidJavaIdentifier(), "a_45_bc")
        assertEquals(CollectionFieldName("a*bc").toValidJavaIdentifier(), "a_42_bc")
        assertEquals(CollectionFieldName("a|bc").toValidJavaIdentifier(), "a_124_bc")
        assertEquals(CollectionFieldName("a|bc").toValidJavaIdentifier(), "a_124_bc")

    }


}
