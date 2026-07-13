package org.axonframework.simple

import org.axonframework.messaging.queryhandling.annotation.QueryHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomerQueryHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  // In-memory "database" – pre-populated with a few customers
  private val customers: Map<String, CustomerDto> = mapOf(
    "1" to CustomerDto("1", "Alice"),
    "2" to CustomerDto("2", "Bob"),
    "3" to CustomerDto("3", "Charlie"),
  )

  @QueryHandler
  fun handle(query: CustomerFindOneQuery): Optional<CustomerDto> {
    log.info("[server5] Handling CustomerFindOneQuery for customerId={}", query.customerId)
    val result = Optional.ofNullable(customers[query.customerId])
    log.info("[server5] Result: {}", result.orElse(null) ?: "<not found>")
    return result
  }
}
