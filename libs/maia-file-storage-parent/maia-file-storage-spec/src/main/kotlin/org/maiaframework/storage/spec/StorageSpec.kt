package org.maiaframework.storage.spec

import org.maiaframework.domain.persist.SchemaName
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes

@Suppress("unused", "MemberVisibilityCanBePrivate")
class StorageSpec : AbstractSpec(appKey = AppKey("storage"), defaultSchemaName = SchemaName("storage")) {


    val md5CheckSumStringType = stringType("org.maiaframework.domain.types.Md5Checksum") {
        provided()
    }


    val fileNameStringType = stringType("org.maiaframework.domain.types.FileName") {
        provided()
    }


    val contentTypeStringType = stringType("org.maiaframework.domain.types.ContentType") {
        provided()
    }


    val fileStorageEntityDef = entity("org.maiaframework.storage", "FileStorageEntry") {
        field("fileName", fileNameStringType) {
            lengthConstraint(max = 100)
        }
        field("lengthInBytes", FieldTypes.long)
        field("fileTimestampUtc", FieldTypes.instant)
        field("description", FieldTypes.string) {
            nullable()
            lengthConstraint(max = 500)
        }
        field("contentType", contentTypeStringType) {
            lengthConstraint(max = 100)
        }
        field("md5", md5CheckSumStringType) {
            lengthConstraint(max = 100)
        }
    }


}
