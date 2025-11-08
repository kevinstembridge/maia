package org.maiaframework.domain

data class EntityClassAndId(val entityClass: Class<*>, val id: Any) {


    override fun toString(): String {
        return "[${this.entityClass}:${this.id}]"
    }


}
