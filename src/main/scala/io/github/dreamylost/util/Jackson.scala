package io.github.dreamylost.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.ScalaObjectMapper

/** @author li.guobin@immomo.com
  * @version 1.0,2021/11/25
  */
object Jackson {

  final val mapper: ObjectMapper with ScalaObjectMapper = {
    val objectMapper = new ObjectMapper() with ScalaObjectMapper
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    objectMapper.registerModule(DefaultScalaModule)
    objectMapper
  }

}
