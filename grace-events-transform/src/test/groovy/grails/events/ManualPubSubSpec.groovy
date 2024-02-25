package grails.events

import grails.events.bus.EventBusAware
import spock.lang.Specification

import javax.annotation.PostConstruct
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by graemerocher on 03/04/2017.
 */
class ManualPubSubSpec extends Specification {

    void "test pub/sub with default event bus"() {
        given:
        SumService sumService = new SumService()
        TotalService totalService = new TotalService()
        EventBusAware annotatedSubscriber = (EventBusAware)totalService
        EventBusAware publisher = (EventBusAware)sumService
        annotatedSubscriber.setTargetEventBus(publisher.getEventBus())
        totalService.init()

        when:
        sumService.sum(1,2)
        sumService.sum(1,2)

        then:
        totalService.total.intValue() == 6
    }
}

// tag::publisher[]
class SumService implements EventPublisher {
    int sum(int a, int b) {
        int result = a + b
        notify("sum", result)
        return result
    }
}
// end::publisher[]

// tag::subscriber[]
class TotalService implements EventBusAware {
    AtomicInteger total = new AtomicInteger(0)

    @PostConstruct
    void init() {
        eventBus.subscribe("sum") { int num ->
            total.addAndGet(num)
        }
    }
}
// end::subscriber[]
