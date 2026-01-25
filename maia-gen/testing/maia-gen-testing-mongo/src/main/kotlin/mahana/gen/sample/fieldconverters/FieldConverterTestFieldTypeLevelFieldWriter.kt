package org.maiaframework.gen.sample.fieldconverters

import org.springframework.stereotype.Component
import java.util.*


@Component
class FieldConverterTestFieldTypeLevelFieldWriter {


    private var value = UUID.randomUUID().toString()


    fun setNextValue(value: String) {

        this.value = value

    }


    fun writeField(inputValue: Any): String {

        return inputValue.toString() + this.value

    }


}
