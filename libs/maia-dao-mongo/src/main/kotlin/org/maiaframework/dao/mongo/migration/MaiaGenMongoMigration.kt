package org.maiaframework.dao.mongo.migration

interface MaiaGenMongoMigration {


    val changeDescription: String


    fun applyMigration()


}
