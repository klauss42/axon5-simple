package org.axonframework.simple

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Client4Application

fun main(args: Array<String>) {
  SpringApplication.run(Client4Application::class.java, *args)
}
