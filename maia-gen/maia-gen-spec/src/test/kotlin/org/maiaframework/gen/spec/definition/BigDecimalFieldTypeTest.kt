package org.maiaframework.gen.spec.definition

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.jdbc.JdbcCompatibleType


class BigDecimalFieldTypeTest {


    @Test
    fun `default is precision 19 and scale 4`() {

        val fieldType = FieldTypes.bigDecimal

        assertThat(fieldType.precision).isEqualTo(19)
        assertThat(fieldType.scale).isEqualTo(4)
        assertThat(fieldType.sqlType).isEqualTo("NUMERIC(19,4)")
        assertThat(fieldType.jdbcCompatibleType).isEqualTo(JdbcCompatibleType.decimal)
        assertThat(fieldType.fqcn.toString()).isEqualTo("java.math.BigDecimal")

    }


    @Test
    fun `explicit precision and scale are carried in the sql type`() {

        val fieldType = FieldTypes.bigDecimal(10, 2)

        assertThat(fieldType.sqlType).isEqualTo("NUMERIC(10,2)")

    }


    @Test
    fun `field types are not singletons`() {

        assertThat(FieldTypes.bigDecimal(10, 2)).isNotEqualTo(FieldTypes.bigDecimal)
        assertThat(FieldTypes.bigDecimal(10, 2)).isEqualTo(FieldTypes.bigDecimal(10, 2))

    }


    @Test
    fun `scale equal to precision and zero scale are valid`() {

        assertThat(FieldTypes.bigDecimal(5, 5).sqlType).isEqualTo("NUMERIC(5,5)")
        assertThat(FieldTypes.bigDecimal(5, 0).sqlType).isEqualTo("NUMERIC(5,0)")

    }


    @Test
    fun `precision below 1 is rejected`() {

        assertThatThrownBy { FieldTypes.bigDecimal(0, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("precision")

    }


    @Test
    fun `scale outside 0 to precision is rejected`() {

        assertThatThrownBy { FieldTypes.bigDecimal(5, -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("scale")

        assertThatThrownBy { FieldTypes.bigDecimal(5, 6) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("scale")

    }


}
