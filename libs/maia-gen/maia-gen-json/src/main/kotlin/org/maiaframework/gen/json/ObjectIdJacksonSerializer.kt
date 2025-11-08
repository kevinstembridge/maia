package org.maiaframework.gen.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.bson.types.ObjectId

class ObjectIdJacksonSerializer: StdSerializer<ObjectId>(ObjectId::class.java) {


    override fun serialize(value: ObjectId, jsonGenerator: JsonGenerator, provider: SerializerProvider) {

        jsonGenerator.writeString(value.toHexString());

    }


}
