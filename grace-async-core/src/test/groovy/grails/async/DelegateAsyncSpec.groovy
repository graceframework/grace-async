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

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import grails.async.decorator.PromiseDecorator
import grails.async.decorator.PromiseDecoratorProvider

/**
 * @author Graeme Rocher
 * @author Michael Yan
 * @since 2.3
 */
class DelegateAsyncSpec extends Specification {

    void "Test delegate async applied to class with a method taking arguments"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'The DelegateAsync annotation is applied to a class.'
        def mathService = new AsyncMathService()
        def p = mathService.sum(1, 2)

        then: 'Methods from the delegate return a promise'
        p instanceof Promise

        when: 'The the value of the promise is obtained'
        def val = p.get()

        then: 'It is correct'
        conditions.eventually {
            val == 3
        }
    }

    void "Test delegate async applied to field with a method taking arguments"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'The DelegateAsync annotation is applied to a class.'
        def mathService = new AsyncMathService2()
        def p = mathService.sum(1, 2)

        then: 'Methods from the delegate return a promise'
        p instanceof Promise

        when: 'The the value of the promise is obtained'
        def val = p.get()

        then: 'It is correct'
        conditions.eventually {
            val == 3
        }
    }

    void "Test delegate async passes decorators to created promises if target is a DecoratorProvider"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when: 'The DelegateAsync annotation is applied to a class.'
        def mathService = new AsyncMathService3()
        def p = mathService.sum(1, 2)

        then: 'Methods from the delegate return a promise'
        p instanceof Promise

        when: 'The the value of the promise is obtained'
        def val = p.get()

        then: 'The decorator is applied to the value'
        conditions.eventually {
            val == 6
        }
    }

}

class MathService {

    Integer sum(int n1, int n2) {
        n1 + n2
    }

    void calculate() {
        // no-op
    }

    // having this method here makes sure that the
    // transformation can deal with copying parameters
    // that are generics placeholders
    <T> void someMethod(T arg) {
    }

}

@DelegateAsync(MathService)
class AsyncMathService {

}

class AsyncMathService2 {

    @DelegateAsync
    MathService ms = new MathService()

}

@DelegateAsync(MathService)
class AsyncMathService3 implements PromiseDecoratorProvider {

    List<PromiseDecorator> decorators = [{ Closure c ->
        return { c.call(*it) * 2 }
                                         } as PromiseDecorator]

}

