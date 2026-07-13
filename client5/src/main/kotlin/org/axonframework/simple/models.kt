package org.axonframework.simple

// Same FQCN as server5 — Axon resolves the query name to
// "org.axonframework.simple.CustomerFindOneQuery" on both sides.
data class CustomerFindOneQuery(val customerId: String)

data class CustomerDto(
  val customerId: String,
  val name: String,
)
