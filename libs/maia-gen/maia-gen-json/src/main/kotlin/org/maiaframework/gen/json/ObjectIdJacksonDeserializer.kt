package org.maiaframework.gen.json

import com.fasterxml.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer
import org.bson.types.ObjectId

class ObjectIdJacksonDeserializer: StdDeserializer<ObjectId>(ObjectId::class.java) {


    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ObjectId {

        return ObjectId(p.valueAsString)

    }


}
