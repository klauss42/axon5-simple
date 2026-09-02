package org.axonframework.simple.config

import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager
import io.axoniq.framework.axonserver.connector.event.AggregateBasedAxonServerEventStorageEngine
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.messaging.eventhandling.conversion.EventConverter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AxonConfig {
  @Bean
  @ConditionalOnProperty(name = ["axon.axonserver.enabled"], matchIfMissing = true)
  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  fun eventStorageEngine(
    connectionManager: AxonServerConnectionManager,
    eventConverter: EventConverter
  ): EventStorageEngine {
    return AggregateBasedAxonServerEventStorageEngine(connectionManager.connection, eventConverter)
  }
}
