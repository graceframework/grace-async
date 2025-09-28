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

import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * @author Graeme Rocher
 * @author Michael Yan
 * @since 2.3
 */
class PromiseMapSpec extends Specification{

    void "Test PromiseMap with mixture of normal entries and promises populated via constructor"() {
        given:
            def conditions = new PollingConditions(timeout: 2)

        when:"A promise map is used with an onComplete handler"
            def map = new PromiseMap<String, Integer>(one:{1}, four:4, eight:{4*2})
            Map<String, Integer> result
            map.onComplete { Map<String, Integer> m ->
                result = m
            }

        then:"An appropriately populated map is returned to the onComplete event"
        conditions.eventually {
            result != null
            result["one"] == 1
            result["four"] == 4
            result["eight"] == 8
        }
    }
    void "Test PromiseMap with mixture of normal entries and promises"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when:"A promise map is used with an onComplete handler"
            def map = new PromiseMap<String, Integer>()
            map["one"] = { 1 }
            map["four"] = 4
            map["eight"] = { 4 * 2 }

            Map<String, Integer> result
            map.onComplete { Map<String, Integer> m ->
                result = m
            }

        then:"An appropriately populated map is returned to the onComplete event"
        conditions.eventually {
            result != null
            result["one"] == 1
            result["four"] == 4
            result["eight"] == 8
        }
    }

    void "Test that a PromiseMap populates values from promises onComplete"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when:"A promise map is used with an onComplete handler"
            def map = new PromiseMap<String, Integer>()
            map["one"] = { 1 }
            map["four"] = { 2 + 2 }
            map["eight"] = { 4 * 2 }

            Map<String, Integer> result
            map.onComplete { Map<String, Integer> m ->
                result = m
            }

        then:"An appropriately populated map is returned to the onComplete event"
        conditions.eventually {
            result != null
            result["one"] == 1
            result["four"] == 4
            result["eight"] == 8
        }
    }


    void "Test that a PromiseMap triggers onError for an exception and ignoresonComplete"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when:"A promise map is used with an onComplete handler"
            def map = new PromiseMap<String, Integer>()
            map["one"] = { 1 }
            map["four"] = { throw new RuntimeException("bad") }
            map["eight"] = { 4 * 2 }

            Map<String, Integer> result
            Throwable err
            map.onComplete { Map<String, Integer> m ->
                result = m
            }
            map.onError {
                err = it
            }

        then:"An appropriately populated map is returned to the onComplete event"
        conditions.eventually {
            result == null
            err != null
            err.message == "java.lang.RuntimeException: bad"
        }
    }

    @Ignore
    void "Test PromiseMap with then chaining"() {
        given:
        def conditions = new PollingConditions(timeout: 2)

        when:"A promise map is used with then chaining"
            def map = new PromiseMap<String, Integer>()
            map["one"] = { 1 }
            def promise = map.then {
                println it
                it['four'] = 4; it
            }.then {
                println it
                it['eight'] = 8; it
            }
            def result = promise.get()
        then:"An appropriately populated map is returned to the onComplete event"
        conditions.eventually {
            result != null
            result["one"] == 1
            result["four"] == 4
            result["eight"] == 8
        }
    }
}
