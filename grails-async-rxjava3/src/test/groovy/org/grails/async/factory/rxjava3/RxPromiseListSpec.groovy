package org.grails.async.factory.rxjava3

import grails.async.PromiseList
import spock.lang.Specification

class RxPromiseListSpec extends Specification{


    void "Test promise list handling"() {
        when:"A list of promises is created"
        def list = new PromiseList()
        list << { 1 }
        list << { 2 }
        list << { 3 }
        def res
        list.onComplete { List results ->
            res = results
        }
        sleep 500

        then:'then the result from onComplete is correct'
        res == [1,2,3]
    }

    void "Test promise list handling with some async operations and some values"() {
        when:"A list of promises is created"
        def list = new PromiseList()
        list << { 1 }
        list <<  2
        list << { 3 }
        def res
        list.onComplete { List results ->
            res = results
        }
        sleep 500

        then:'then the result from onComplete is correct'
        res == [1,2,3]
    }

    void "Test promise list with then chaining"() {
        when:"A promise list is used with then chaining"
        def list = new PromiseList<Integer>()
        list << { 1 }
        def promise = list
                .then {
            it << 2; it
        }
        .then {
            Thread.dumpStack()
            it << 3; it
        }
        def result = promise.get()
        then:"An appropriately populated list is produced"
        result == [1,2,3]

    }

    void "Test promise list with an exception"() {
        when:"A promise list with a promise that throws an exception"
        def list = new PromiseList()
        list << {
            1
        }
        list << {
            throw new RuntimeException("bad")
        }
        list << {
            3
        }
        def res
        list.onComplete { List results ->
            res = results
        }
        Throwable err
        list.onError { Throwable t ->
            err = t
        }.get()

        list.get()

        then:'the onError handler is invoked with the exception'
        thrown(RuntimeException)
        err != null
        err.message == "bad"
        res == null
    }
}

