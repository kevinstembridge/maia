package org.maiaframework.gen


import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

import java.io.IOException


@Configuration
class MaiaGenTestingSpringConfiguration {


    @Bean
    fun jdk8Module(): Jdk8Module {

        return Jdk8Module()

    }


    @Bean
    fun javaTimeModule(): JavaTimeModule {

        return JavaTimeModule()

    }


    @Bean
    fun kotlinModule(): KotlinModule {

        return KotlinModule()

    }


    @Bean
    fun objectIdModule(): Module {

        val simpleModule = SimpleModule("ObjectIdModule", Version(1, 0, 0, null, "org.maiaframework", "maia"))

        simpleModule.addSerializer(ObjectId::class.java, object : JsonSerializer<ObjectId>() {
            @Throws(IOException::class)
            override fun serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider) {
                gen.writeString(value.toHexString())
            }
        })


        simpleModule.addDeserializer(ObjectId::class.java, object : JsonDeserializer<ObjectId>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctx: DeserializationContext): ObjectId {
                return ObjectId(p.valueAsString)
            }
        })

        return simpleModule

    }


    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {

        return Jackson2ObjectMapperBuilderCustomizer{ jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    }

    companion object {


        @Bean
        fun propertyPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer {

            return PropertySourcesPlaceholderConfigurer()

        }
    }


}
