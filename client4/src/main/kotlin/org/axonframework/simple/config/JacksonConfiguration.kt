package org.axonframework.simple.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.axonframework.serialization.ChainingConverter
import org.axonframework.serialization.RevisionResolver
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfiguration : BeanClassLoaderAware {
  private var beanClassLoader: ClassLoader? = null

  @Primary
  @Bean
  fun serializer(revisionResolver: RevisionResolver?): Serializer {
    val converter = ChainingConverter(beanClassLoader)
    return JacksonSerializer.builder()
      .revisionResolver(revisionResolver)
      .converter(converter)
      // create a new mapper instance for Axon, as the JacksonSerializer changes the instance
      .objectMapper(createMapperInstance())
      .defaultTyping()
      .build()
  }

  override fun setBeanClassLoader(classLoader: ClassLoader) {
    beanClassLoader = classLoader
  }

  @Bean
  fun objectMapper(): ObjectMapper {
    // this mapper instance will be used by Spring
    return createMapperInstance()
  }

  private fun createMapperInstance(): ObjectMapper {
    return JsonMapper.builder()
      .addModule(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
      .addModule(Jdk8Module())
      .build()
      .findAndRegisterModules() //Registers all modules on classpath
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      // needed to be able to serialize/deserialize commands/events/queries that do not use Java-beans
      // getter's but instead fluent style accessors or private Kotlin data class fields
      .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
  }
}
