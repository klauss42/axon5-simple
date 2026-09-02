package org.axonframework.simple.config

import org.axonframework.conversion.DelegatingGeneralConverter
import org.axonframework.conversion.GeneralConverter
import org.axonframework.conversion.jackson.JacksonConverter
import org.axonframework.messaging.core.conversion.DelegatingMessageConverter
import org.axonframework.messaging.core.conversion.MessageConverter
import org.axonframework.messaging.eventhandling.conversion.DelegatingEventConverter
import org.axonframework.messaging.eventhandling.conversion.EventConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary


@Configuration
class ConverterConfiguration {

  @Bean
  @Primary
  fun converter(): GeneralConverter =
    DelegatingGeneralConverter(JacksonConverter(JacksonConfiguration.createJsonMapperInstance(true)))

  @Bean
  fun messageConverter(generalConverter: GeneralConverter): MessageConverter =
    DelegatingMessageConverter(generalConverter)

  @Bean
  fun eventConverter(messageConverter: MessageConverter): EventConverter =
    DelegatingEventConverter(messageConverter)
}
