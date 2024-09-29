package io.github.positionpal.location.infrastructure.services

import akka.serialization.jackson.JacksonObjectMapperFactory
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class CustomJacksonObjectMapperFactory extends JacksonObjectMapperFactory:
  override def newObjectMapper(bindingName: String, jsonFactory: JsonFactory): ObjectMapper =
    val mapper = super.newObjectMapper(bindingName, jsonFactory)
    mapper.registerModule(DefaultScalaModule)
    mapper
