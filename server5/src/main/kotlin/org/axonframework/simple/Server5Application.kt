package org.axonframework.simple

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class Server5Application

fun main(args: Array<String>) {
  SpringApplication.run(Server5Application::class.java, *args)
}
