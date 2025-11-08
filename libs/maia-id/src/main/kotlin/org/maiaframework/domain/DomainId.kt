package org.maiaframework.domain

import com.fasterxml.uuid.Generators
import org.maiaframework.types.StringType


class DomainId(value: String): StringType<DomainId>(value) {


    companion object {


        fun newId(): DomainId {

            // A version 7 (time-based) UUID with per-call random values.
            // We use a time-based id to improve index insertion performance.
            // When a value is inserted into a B-Tree index, the tree is rebalanced.
            // This rebalancing operation is much faster if values are sequential.
            val uuid = Generators.timeBasedEpochRandomGenerator().generate()
            return DomainId(uuid.toString())

        }


    }


}
