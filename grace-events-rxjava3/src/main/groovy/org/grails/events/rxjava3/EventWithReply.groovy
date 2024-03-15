package org.grails.events.rxjava3

import grails.events.Event
import groovy.transform.CompileStatic

/**
 * An event with a reply
 *
 * @author Michael Yan
 * @since 5.2
 */
@CompileStatic
class EventWithReply {
    final Event event
    final Closure reply

    EventWithReply(Event event, Closure reply) {
        this.event = event
        this.reply = reply
    }
}
