package nl.kick.httpheaders

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HttpHeadersApplication

fun main(args: Array<String>) {
	runApplication<HttpHeadersApplication>(*args)
}
