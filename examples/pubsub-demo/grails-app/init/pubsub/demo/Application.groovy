package pubsub.demo

import grails.boot.Grails
import org.springframework.context.annotation.ComponentScan

@ComponentScan
class Application {
    static void main(String[] args) {
        Grails.run(Application, args)
    }
}