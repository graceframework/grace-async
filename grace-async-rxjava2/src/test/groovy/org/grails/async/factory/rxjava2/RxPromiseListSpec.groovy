/*
 * Copyright 2013-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.async.factory.rxjava2

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import grails.async.PromiseList

/**
 * @author Graeme Rocher
 * @author Michael Yan
 * @since 2.3
 */
class RxPromiseListSpec extends Specification {

    void "Test promise list handling"() {
        def conditions = new PollingConditions(timeout: 10)
        when: 'A list of promises is created'
        def list = new PromiseList()
        list << { 1 }
        list << { 2 }
        list << { 3 }
        def res
        list.onComplete { List results ->
            res = results
        }

        then: 'then the result from onComplete is correct'
        conditions.eventually {
            res == [1, 2, 3]
        }
    }

    void "Test promise list handling with some async operations and some values"() {
        def conditions = new PollingConditions(timeout: 10)
        when: 'A list of promises is created'
        def list = new PromiseList()
        list << { 1 }
        list << 2
        list << { 3 }
        def res
        list.onComplete { List results ->
            res = results
        }

        then: 'then the result from onComplete is correct'
        conditions.eventually {
            res == [1, 2, 3]
        }
    }

    void "Test promise list with then chaining"() {
        def conditions = new PollingConditions(timeout: 10)

        when: 'A promise list is used with then chaining'
        def list = new PromiseList<Integer>()
        list << { 1 }
        def promise = list
                .then {
                    it << 2; it
                }
                .then {
                    // Thread.dumpStack()
                    it << 3; it
                }
        def result = promise.get()

        then: 'An appropriately populated list is produced'
        conditions.eventually {
            result == [1, 2, 3]
        }
    }

    void "Test promise list with an exception"() {
        def conditions = new PollingConditions(timeout: 10)

        when: 'A promise list with a promise that throws an exception'
        def list = new PromiseList()
        list << {
            1
        }
        list << {
            throw new RuntimeException('bad')
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

        then: 'the onError handler is invoked with the exception'
        thrown(RuntimeException)
        conditions.eventually {
            err != null
            err.message == 'bad'
            res == null
        }
    }

}
