package org.maiaframework.dao.mongo.gridfs

import org.maiaframework.common.BlankStringException
import org.maiaframework.domain.types.ContentType
import org.maiaframework.domain.types.FileName
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSFile
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import org.bson.Document
import org.bson.types.ObjectId
import java.io.InputStream

class GridFsFacade(mongoClient: MongoClient, defaultDatabaseName: String) {


    private val defaultDatabase: MongoDatabase
    val defaultGridFS: GridFSBucket


    init {

        BlankStringException.throwIfBlank(defaultDatabaseName, "defaultDatabaseName")

        this.defaultDatabase = mongoClient.getDatabase(defaultDatabaseName)
        this.defaultGridFS = GridFSBuckets.create(mongoClient.getDatabase(defaultDatabaseName))

    }


    fun saveToGridFS(filename: FileName, contentType: ContentType, inputStream: InputStream): ObjectId {

        val options = GridFSUploadOptions()
                .metadata(Document("contentType", contentType.value))

        return defaultGridFS.uploadFromStream(filename.value, inputStream, options)

    }


    fun getGridFsFileAndDownloadStream(gridFsId: ObjectId): GridFsFileAndDownloadStream? {

        val query = Document("_id", gridFsId)
        val gridFSFindIterable = this.defaultGridFS.find(query)
        val gridFSFile: GridFSFile = gridFSFindIterable.first() ?: return null
        val downloadStream = this.defaultGridFS.openDownloadStream(gridFSFile.objectId)

        return GridFsFileAndDownloadStream(gridFSFile, downloadStream)

    }


    fun delete(gridFsId: ObjectId) {

        this.defaultGridFS.delete(gridFsId)

    }


}
