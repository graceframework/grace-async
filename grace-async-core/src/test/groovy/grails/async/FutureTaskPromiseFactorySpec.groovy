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
package grails.async

import java.util.concurrent.ExecutionException

import spock.lang.Issue
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import grails.async.decorator.PromiseDecorator
import org.grails.async.factory.future.CachedThreadPoolPromiseFactory

/**
 * @author Graeme Rocher
 * @author Michael Yan
 * @since 2.3
 */
class FutureTaskPromiseFactorySpec extends Specification {

    void setup() {
        Promises.promiseFactory = new CachedThreadPoolPromiseFactory()
    }

    void cleanup() {
        Promises.promiseFactory = null
    }

    void "Test add promise decorator"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A decorator is added'
        def decorator = { Closure c ->
            return { "*${c.call(*it)}*" }
        } as PromiseDecorator

        def p = Promises.createPromise({ 10 }, [decorator])
        def result = p.get()

        then: 'The result is decorate'
        conditions.eventually {
            result == '*10*'
        }
    }

    void "Test promise map handling"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A promise map is created'
        def map = Promises.createPromise(one: { 1 }, two: { 1 + 1 }, four: { 2 * 2 })
        def result = map.get()

        then: 'The map is valid'
        conditions.eventually {
            result == [one: 1, two: 2, four: 4]
        }
    }

    void "Test promise list handling"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A promise list is created from two promises'
        def p1 = Promises.createPromise { 1 + 1 }
        def p2 = Promises.createPromise { 2 + 2 }
        def list = Promises.createPromise(p1, p2)

        def result
        list.onComplete { List v ->
            result = v
        }

        then: 'The result is correct'
        conditions.eventually {
            result == [2, 4]
        }

        when: 'A promise list is created from two closures'
        list = Promises.createPromise({ 1 + 1 }, { 2 + 2 })

        list.onComplete { List v ->
            result = v
        }

        then: 'The result is correct'
        conditions.eventually {
            result == [2, 4]
        }
    }

    void "Test promise onComplete handling"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A promise is executed with an onComplete handler'
        def promise = Promises.createPromise { 1 + 1 }
        def result
        def hasError = false
        promise.onComplete { val ->
            result = val
        }.get()
        promise.onError {
            hasError = true
        }.get()

        then: 'The onComplete handler is invoked and the onError handler is ignored'
        conditions.eventually {
            result == 2
            hasError == false
        }
    }

    void "Test promise onError handling"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A promise is executed with an onComplete handler'
        def promise = Promises.createPromise {
            throw new RuntimeException('bad')
        }
        def result
        Throwable error
        promise.onComplete { val ->
            result = val
        }
        promise.onError { err ->
            error = err
        }.get()

        then: 'The onComplete handler is invoked and the onError handler is ignored'
        thrown(ExecutionException)
        conditions.eventually {
            result == null
            error != null
        }
    }

    void "Test promise chaining"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A promise is chained'
        def promise = Promises.createPromise { 1 + 1 }
        promise = promise.then { it * 2 } then { it + 6 }
        def val = promise.get()

        then: 'the chain is executed'
        conditions.eventually {
            val == 10
        }
    }

    void "Test promise chaining with exception"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'A promise is chained'
        def promise = Promises.createPromise { 1 + 1 }
        promise = promise.then { it * 2 } then { throw new RuntimeException('bad') } then { it + 6 }
        def val = promise.get()

        then: 'the chain is executed'
        thrown RuntimeException
        conditions.eventually {
            val == null
        }
    }

    @Issue('GRAILS-10152')
    void "Test promise closure is not executed multiple times if it returns null"() {
        given:
        def conditions = new PollingConditions(timeout: 2)
        Closure callable = Mock(Closure) {
            call() >> null
        }

        when: 'A promise is created'
        Promises.waitAll([Promises.createPromise(callable), Promises.createPromise(callable)])

        then: 'the closure is executed twice'
        conditions.eventually {
            2 * callable.call()
        }
    }

}
