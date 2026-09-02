package org.axonframework.simple.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule


@Configuration
class JacksonConfiguration {

  @Bean
  fun jsonMapper(): JsonMapper {
    // this mapper instance will be used by Spring
    return createJsonMapperInstance()
  }

  companion object {
    private fun defaultJsonMapperBuilder(): JsonMapper.Builder =
      JsonMapper.builder()
        .addModule(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
        .findAndAddModules()
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // needed to be able to serialize/deserialize commands/events/queries that do not use Java-beans
        // getters but instead fluent style accessors or private Kotlin data class fields
        .changeDefaultVisibility { it.withFieldVisibility(JsonAutoDetect.Visibility.ANY) }

    fun createJsonMapperInstance(withDefaultTyping: Boolean = false): JsonMapper =
      if (withDefaultTyping) {
        defaultJsonMapperBuilder()
          // Default typing (OBJECT_AND_NON_CONCRETE + WRAPPER_ARRAY) is required to read events written by the
          // legacy Axon 4 application. Those payloads wrap non-concrete-typed fields (e.g., List<String>, Map<...>)
          // with a "[<class>, <value>]" header, e.g. "roles": ["java.util.ArrayList", ["A", "B"]]. Without this
          // setting, the new JacksonConverter throws ConversionException on every event, whose declared field type
          // is an interface/abstract class. New events written by Axon 5 will use the same format, so old and
          // new payloads are wire-compatible.
          // The same converter is used by Axon's JpaTokenStore; the legacy AF4 __config token in tokenentry is
          // also stored in the WRAPPER_ARRAY form (see V1.011 migration).
          .activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder().allowIfBaseType(Any::class.java).build(),
            DefaultTyping.OBJECT_AND_NON_CONCRETE,
            JsonTypeInfo.As.WRAPPER_ARRAY
          ).build()
      } else {
        defaultJsonMapperBuilder().build()
      }
  }
}
