package org.maiaframework.dao.mongo.gridfs

import com.mongodb.client.gridfs.model.GridFSFile
import java.io.InputStream

data class GridFsFileAndDownloadStream(
        val gridFsFile: GridFSFile,
        val downloadStream: InputStream)
