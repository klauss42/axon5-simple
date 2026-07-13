package org.axonframework.simple

/**
 * No @Query annotation intentionally: Axon 5 falls back to FQCN as the query name,
 * which matches what Axon 4 clients send by default — enabling cross-version dispatch.
 * Query name resolved by both frameworks: "org.axonframework.simple.CustomerFindOneQuery"
 */
data class CustomerFindOneQuery(val customerId: String)

data class CustomerDto(
  val customerId: String,
  val name: String,
)
