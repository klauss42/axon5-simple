package org.axonframework.simple

data class CustomerFindOneQuery(val customerId: String)
class CustomerFindAllQuery
class CustomerFindPageQuery

data class CustomerDto(
  val customerId: String,
  val name: String,
)
