package org.maiaframework.http


import org.maiaframework.types.StringType
import org.apache.hc.core5.http.NameValuePair
import org.apache.hc.core5.http.message.BasicNameValuePair

import java.util.ArrayList
import java.util.LinkedList


class NameValuePairBuilder {


    private val nameValuePairs = LinkedList<NameValuePair>()


    constructor() {
        // do nothing
    }


    constructor(fieldName: String, fieldValue: String) {

        add(fieldName, fieldValue)

    }


    fun add(fieldName: String, stringType: StringType<*>): NameValuePairBuilder {

        return add(fieldName, stringType.value)

    }


    fun add(fieldName: String, fieldValue: String): NameValuePairBuilder {

        this.nameValuePairs.add(BasicNameValuePair(fieldName, fieldValue))
        return this

    }


    fun addAll(nameValuePairs: List<NameValuePair>): NameValuePairBuilder {

        this.nameValuePairs.addAll(nameValuePairs)
        return this

    }


    fun build(): List<NameValuePair> {

        return ArrayList(this.nameValuePairs)

    }


}
