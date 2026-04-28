package pubsub.demo

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import spock.lang.Specification

import grails.testing.mixin.integration.Integration

@Integration
class TaskControllerSpec extends Specification {

    @LocalServerPort
    private int port

    @Autowired
    private TestRestTemplate restTemplate

    void "test async error handling"() {
        when: "The home page is requested"
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/task/error", String)

        then: "The response is 500"
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        response.body == 'error occured'
    }

}
