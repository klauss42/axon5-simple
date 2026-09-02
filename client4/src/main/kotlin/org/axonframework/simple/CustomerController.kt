package org.axonframework.simple

import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
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
      .doOnNext { log.info("[client4] Query result: {}", it) }
      .map { opt ->
        if (opt.isPresent) ResponseEntity.ok(opt.get())
        else ResponseEntity.notFound().build()
      }
  }

  @GetMapping
  fun getAll(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client4] Dispatching CustomerFindAllQuery")
    return Mono.fromFuture(
      queryGateway.query(
        CustomerFindAllQuery(),
        ResponseTypes.multipleInstancesOf(CustomerDto::class.java)
      )
    )
      .doOnNext { log.info("[client4] Query result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }

  @GetMapping("/page")
  fun getPage(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client4] Dispatching CustomerFindPageQuery")
    return Mono.fromFuture(
      queryGateway.query(
        CustomerFindPageQuery(),
        ResponseTypes.multipleInstancesOf(CustomerDto::class.java)
      )
    )
      .doOnNext { log.info("[client4] Query result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }

  @GetMapping("/subscription")
  fun getSubscription(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client4] Dispatching subscriptionQuery")

    val subscriptionQuery = queryGateway.subscriptionQuery(
      CustomerFindAllQuery(),
      ResponseTypes.multipleInstancesOf(CustomerDto::class.java),
      ResponseTypes.instanceOf(CustomerDto::class.java)
    )

    return subscriptionQuery
      .initialResult()
      .doFinally { subscriptionQuery.close() }
      .defaultIfEmpty(emptyList())
      .map { ResponseEntity.ok(it) }

  }

  @GetMapping("/streaming")
  fun getStreaming(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client4] Dispatching streamingQuery")
    val publisher = queryGateway.streamingQuery(
      CustomerFindAllQuery(),
      CustomerDto::class.java
    )
    return Flux
      .from(publisher)
      .doOnNext { log.info("[client4] Streaming next: {}", it) }
      .doFinally { log.info("[client4] Streaming completed") }
      .collectList()
      .doOnSuccess { log.info("[client4] Streaming result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }

}
