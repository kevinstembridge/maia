package org.maiaframework.http

import com.fasterxml.jackson.core.JsonGenerator
import tools.jackson.databind.ObjectMapper
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.IOException
import java.io.OutputStream
import java.io.UncheckedIOException
import java.util.stream.Stream

class JsonStreamingResponseBody<T>(private val objectMapper: ObjectMapper, private val stream: Stream<T>) : StreamingResponseBody {


    override fun writeTo(outputStream: OutputStream) {

        val generator = this.objectMapper.factory.createGenerator(outputStream)

        generator.writeStartArray()
        this.stream.forEach(writeObjectTo(generator))
        generator.writeEndArray()

    }


    private fun writeObjectTo(generator: JsonGenerator): (T) -> Unit {

        return { t ->
            try {
                generator.writeObject(t)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

    }


}
