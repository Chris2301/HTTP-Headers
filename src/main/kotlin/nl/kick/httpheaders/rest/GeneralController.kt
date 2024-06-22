package nl.kick.httpheaders.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/v1")
class GeneralController {
    @GetMapping(path = ["/get/general"], produces = ["application/json"])
    fun getBasic(): String {
        return "This API should always be reachable"
    }
}