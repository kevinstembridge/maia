package org.maiaframework.domain

data class EntityClassAndPk(val entityClass: Class<*>, val primaryKey: Map<String, Any>) {


    override fun toString(): String {
        return "[${this.entityClass}:${this.primaryKey}]"
    }


}
