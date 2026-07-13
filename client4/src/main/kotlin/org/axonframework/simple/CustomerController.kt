package org.axonframework.simple

import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/customer")
class CustomerController(
  private val queryGateway: QueryGateway,
) {

  private val log = LoggerFactory.getLogger(javaClass)

  @GetMapping("/{customerId}")
  fun getCustomer(@PathVariable customerId: String): Mono<ResponseEntity<CustomerDto>> {
    log.info("[client4] Dispatching CustomerFindOneQuery for customerId={}", customerId)
    return Mono.fromFuture(
      queryGateway.query(
        CustomerFindOneQuery(customerId),
        ResponseTypes.optionalInstanceOf(CustomerDto::class.java),
      )
    )
      .doOnNext {
        log.info("[client4] Query result: {}", it.orElse(null))
      }
      .map { opt ->
        if (opt.isPresent) ResponseEntity.ok(opt.get())
        else ResponseEntity.notFound().build()
      }
  }
}
