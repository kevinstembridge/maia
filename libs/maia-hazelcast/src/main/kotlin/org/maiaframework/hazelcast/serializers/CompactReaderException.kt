package org.maiaframework.hazelcast.serializers

class CompactReaderException private constructor(
        message: String,
        val fieldName: String
): RuntimeException(message) {


    companion object {


        fun throwIfNullOrBlank(fieldName: String, value: String?): String {

            if (value.isNullOrBlank()) {
                throw CompactReaderException("Found a null or blank value in required field '$fieldName'", fieldName)
            }

            return value

        }


        fun throwIfNull(fieldName: String, value: Any?) {

            if (value == null) {
                throw CompactReaderException("Found a null value in required field '$fieldName'", fieldName)
            }

        }


    }


}
