package org.maiaframework.storage

import org.maiaframework.domain.DomainId
import org.maiaframework.domain.types.ContentType
import org.maiaframework.domain.types.FileName
import java.io.InputStream
import java.time.Instant

interface FileStorage {


    fun store(
        fileName: FileName,
        contentType: ContentType,
        inputStream: InputStream,
        description: String?,
        fileTimestampUtc: Instant = Instant.now()
    ): FileStorageEntryEntity


    fun fetch(storageEntryId: DomainId): Pair<FileStorageEntryEntity, InputStream>


}
