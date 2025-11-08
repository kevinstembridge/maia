package org.maiaframework.domain.mongo

import org.maiaframework.domain.DomainId
import org.bson.types.ObjectId

fun DomainId.toObjectId() = ObjectId(this.value)
