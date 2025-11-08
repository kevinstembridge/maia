package org.maiaframework.jdbc

import org.maiaframework.domain.EntityClassAndId


class EntityNotFoundException(message: String, val tableName: TableName): RuntimeException(message) {


    constructor(entityClassAndId: EntityClassAndId, tableName: TableName):
        this("Entity [${entityClassAndId}] not found in table [$tableName]", tableName)


}
