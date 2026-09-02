package org.axonframework.simple

import org.axonframework.messaging.queryhandling.gateway.QueryGateway
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
    log.info("[client5] Dispatching CustomerFindOneQuery for customerId={}", customerId)
    return Mono.fromFuture(
      queryGateway.query(
        CustomerFindOneQuery(customerId),
        CustomerDto::class.java
      )
    )
      .doOnNext { log.info("[client5] Query result: {}", it) }
      .doOnSuccess { if (it == null) log.info("[client5] Query returned empty (no customer found for id={})", customerId) }
      .map { ResponseEntity.ok(it) }
      .defaultIfEmpty(ResponseEntity.notFound().build())
  }

  @GetMapping
  fun getAll(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client5] Dispatching CustomerFindAllQuery")
    return Mono.fromFuture(
      queryGateway.queryMany(
        CustomerFindAllQuery(),
        CustomerDto::class.java
      )
    )
      .doOnNext { log.info("[client5] Query result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }

  @GetMapping("/page")
  fun getPage(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client5] Dispatching CustomerFindPageQuery")
    return Mono.fromFuture(
      queryGateway.queryMany(
        CustomerFindPageQuery(),
        CustomerDto::class.java
      )
    )
      .doOnNext { log.info("[client5] Query result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }

  @GetMapping("/subscription")
  fun getSubscription(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client5] Dispatching subscriptionQuery")

    val publisher = queryGateway.subscriptionQuery(
      CustomerFindPageQuery(),
      CustomerDto::class.java
    )
    return Flux
      .from(publisher)
      .doOnNext { log.info("[client5] Subscription next: {}", it) }
      .doFinally { log.info("[client5] Subscription completed") }
      .take(3) // dirty, but just want to complete the stream for this test
      .collectList()
      .doOnSuccess { log.info("[client5] Subscription result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }

  @GetMapping("/streaming")
  fun getStreaming(): Mono<ResponseEntity<List<CustomerDto>>> {
    log.info("[client5] Dispatching streamingQuery")

    val publisher = queryGateway.streamingQuery(
      CustomerFindPageQuery(),
      CustomerDto::class.java
    )
    return Flux
      .from(publisher)
      .doOnNext { log.info("[client5] Streaming next: {}", it) }
      .doFinally { log.info("[client5] Streaming completed") }
      .collectList()
      .doOnSuccess { log.info("[client5] Streaming result: {}", it) }
      .map { ResponseEntity.ok(it) }
  }


}
