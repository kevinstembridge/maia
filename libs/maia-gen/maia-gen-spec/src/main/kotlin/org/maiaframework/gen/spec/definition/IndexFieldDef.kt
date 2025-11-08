package org.maiaframework.gen.spec.definition

import org.maiaframework.domain.persist.SortDirection
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName


class IndexFieldDef(val entityFieldDef: EntityFieldDef, val sortDirection: SortDirection) {


    val databaseColumnName: TableColumnName = this.entityFieldDef.dbColumnFieldDef.tableColumnName


    val isAscending: Boolean = this.sortDirection == SortDirection.asc


}
