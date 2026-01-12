package org.maiaframework.gen.json

import com.fasterxml.jackson.core.JsonGenerator
import tools.jackson.databind.SerializerProvider
import tools.jackson.databind.ser.std.StdSerializer
import org.bson.types.ObjectId

class ObjectIdJacksonSerializer: StdSerializer<ObjectId>(ObjectId::class.java) {


    override fun serialize(value: ObjectId, jsonGenerator: JsonGenerator, provider: SerializerProvider) {

        jsonGenerator.writeString(value.toHexString());

    }


}
