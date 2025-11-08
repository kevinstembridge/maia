package org.maiaframework.csv

interface CsvWriterHelper<T> {


    fun getHeaderNames(): Array<String>


    fun getColumnsFrom(t: T): List<*>


}
