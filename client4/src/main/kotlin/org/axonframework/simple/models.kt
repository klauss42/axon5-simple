package org.axonframework.simple

// Identical package + class names as server5/client5.
// Axon 4 uses FQCN ("org.axonframework.simple.CustomerFindOneQuery") as the query name,
// which matches the Axon 5 server because it also falls back to FQCN (no @Query annotation).
data class CustomerFindOneQuery(val customerId: String)

data class CustomerDto(
  val customerId: String,
  val name: String,
)
