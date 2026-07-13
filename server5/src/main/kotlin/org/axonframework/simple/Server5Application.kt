package org.axonframework.simple

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Server5Application

fun main(args: Array<String>) {
  SpringApplication.run(Server5Application::class.java, *args)
}
