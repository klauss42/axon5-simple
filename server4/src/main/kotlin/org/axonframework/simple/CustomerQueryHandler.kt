package org.axonframework.simple

import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
    log.info("[server4] Handling CustomerFindOneQuery for customerId={}", query.customerId)
    val result = Optional.ofNullable(customers[query.customerId])
    log.info("[server4] Result: {}", result.orElse(null) ?: "<not found>")
    return result
  }

  @QueryHandler
  fun handle(query: CustomerFindAllQuery): List<CustomerDto> {
    log.info("[server4] Handling CustomerFindAllQuery")
    val result = customers.values.toList()
    log.info("[server4] Result: {}", result)
    return result
  }

  @QueryHandler
  fun handle(query: CustomerFindPageQuery): Page<CustomerDto> {
    log.info("[server4] Handling CustomerFindPageQuery")
    val result = customers.values.toList()
    log.info("[server4] Result: {}", result)
    return PageImpl(result)
  }
}
