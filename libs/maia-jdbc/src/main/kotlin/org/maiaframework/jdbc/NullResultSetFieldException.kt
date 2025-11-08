package org.maiaframework.jdbc

class NullResultSetFieldException: MaiaDataAccessException {

    val columnIndex: Int?
    val columnName: String?

    constructor(columnName: String): super("Found a null value in a ResultSet field that is expected to be non-null. columnName='$columnName'") {
        this.columnName = columnName
        this.columnIndex = null
    }

    constructor(columnIndex: Int): super("Found a null value in a ResultSet field that is expected to be non-null. columnIndex='$columnIndex'") {
        this.columnName = null
        this.columnIndex = columnIndex
    }

}
