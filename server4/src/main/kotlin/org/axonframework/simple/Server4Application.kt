package org.axonframework.simple

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class Server4Application

fun main(args: Array<String>) {
  SpringApplication.run(Server4Application::class.java, *args)
}
