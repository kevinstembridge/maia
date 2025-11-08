package org.maiaframework.storage

import jakarta.xml.bind.DatatypeConverter
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.types.ContentType
import org.maiaframework.domain.types.FileName
import org.maiaframework.domain.types.Md5Checksum
import org.apache.commons.io.FileUtils
import org.apache.commons.io.input.BoundedInputStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Instant

@Component
@Profile("fileStorage_local")
class LocalFileStorage(
        @Value("\${maia.file-storage.local.base-dir}") private val baseDir: File,
        private val fileStorageEntryDao: FileStorageEntryDao
): FileStorage {


    private val logger = LoggerFactory.getLogger(LocalFileStorage::class.java)


    override fun store(
        fileName: FileName,
        contentType: ContentType,
        inputStream: InputStream,
        description: String?,
        fileTimestampUtc: Instant
    ): FileStorageEntryEntity {

        val messageDigest = createMessageDigest()
        val digestInputStream = DigestInputStream(inputStream, messageDigest)
        val countingInputStream = BoundedInputStream.builder().setInputStream(digestInputStream).get()

        val storageEntryId = DomainId.newId()
        val outputFile = File(baseDir, storageEntryId.value)

        FileUtils.copyToFile(countingInputStream, outputFile)

        val length = countingInputStream.count
        val md5String = DatatypeConverter.printHexBinary(messageDigest.digest())

        val entryEntity = FileStorageEntryEntity(
                contentType,
                Instant.now(),
                description,
                fileName,
                fileTimestampUtc,
                storageEntryId,
                length,
                Md5Checksum(md5String)
        )

        logger.info("Recording file storage entry with id $storageEntryId for file named $fileName")

        this.fileStorageEntryDao.insert(entryEntity)

        return entryEntity

    }


    override fun fetch(storageEntryId: DomainId): Pair<FileStorageEntryEntity, InputStream> {

        val storageEntryEntity = this.fileStorageEntryDao.findByPrimaryKey(storageEntryId)

        val file = File(this.baseDir, storageEntryId.value)
        val fis = FileInputStream(file)

        return Pair(storageEntryEntity, fis)

    }


    private fun createMessageDigest() = MessageDigest.getInstance("MD5")


}
