package org.maiaframework.jdbc

import org.maiaframework.domain.EntityClassAndPk


class EntityNotFoundException(message: String, val tableName: TableName): RuntimeException(message) {


    constructor(entityClassAndPk: EntityClassAndPk, tableName: TableName):
        this("Entity [${entityClassAndPk}] not found in table [$tableName]", tableName)


}
