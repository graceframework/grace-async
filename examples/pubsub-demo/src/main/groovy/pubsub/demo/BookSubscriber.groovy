package pubsub.demo

import java.util.concurrent.ConcurrentLinkedDeque

import org.springframework.stereotype.Component

import grails.events.annotation.Subscriber
import org.grails.datastore.mapping.engine.event.PreInsertEvent

@Component
class BookSubscriber {

    List<String> newBooks =[]

    @Subscriber("newBook")
    void withBook(Book book) {
        newBooks.add(book.title)
    }

    // tag::gorm[]
    Collection<PreInsertEvent> insertEvents = new ConcurrentLinkedDeque<>()

    @Subscriber
    void beforeInsert(PreInsertEvent event) {
        insertEvents.add(event)
    }
    // end::gorm[]
}
