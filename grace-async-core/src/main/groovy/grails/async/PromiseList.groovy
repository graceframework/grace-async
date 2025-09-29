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

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic

/**
 * A list of promises
 *
 * @author Graeme Rocher
 * @since 2.3
 */
@CompileStatic
class PromiseList<T> implements Promise<List<T>> {

    protected List<Promise> promises = []
    protected List<T> initialized

    @Override
    Promise<List<T>> accept(List<T> value) {
        initialized = value
        return this
    }

    /**
     * Add a promise to the promise list
     *
     * @param callable The callable
     * @return The promise list
     */
    PromiseList leftShift(Closure callable) {
        promises << Promises.createPromise(callable)
        return this
    }

    /**
     * Add a value as a bound promise to the list of values
     *
     * @param callable The callable
     * @return The promise list
     */
    PromiseList leftShift(value) {
        promises << Promises.createBoundPromise(value)
        return this
    }

    /**
     * Add a promise to the promise list
     *
     * @param callable The callable
     * @return The promise list
     */
    PromiseList leftShift(Promise p) {
        promises << p
        return this
    }

    /**
     * Implementation of add that adds any value as a bound promise
     * @param callable The callable
     * @return True if it was added
     */
    boolean add(value) {
        return promises.add(Promises.createBoundPromise(value))
    }

    /**
     * Implementation of add that takes a closure and creates a promise, adding it to the list
     * @param callable The callable
     * @return True if it was added
     */
    boolean add(Closure callable) {
        return promises.add(Promises.createPromise(callable))
    }

    /**
     * Implementation of add that takes a promise, adding it to the list
     * @param callable The callable
     * @return True if it was added
     */
    boolean add(Promise<T> p) {
        return promises.add(p)
    }

    /**
     * Execute the given closure when all promises are complete
     *
     * @param callable The callable
     */
    @Override
    @SuppressWarnings('unchecked')
    Promise onComplete(Closure callable) {
        return Promises.onComplete(promises, callable)
    }

    @Override
    @SuppressWarnings('unchecked')
    Promise onError(Closure callable) {
        return Promises.onError(promises, callable)
    }

    @Override
    @SuppressWarnings('unchecked')
    Promise then(Closure callable) {
        Promises.onComplete(promises, { List values -> values })
                .then(callable)
    }

    /**
     * Synchronously obtains all the values from all the promises
     * @return The values
     */
    @Override
    boolean cancel(boolean mayInterruptIfRunning) {
        return false
    }

    @Override
    boolean isCancelled() {
        return false
    }

    @Override
    boolean isDone() {
        return promises.every { Promise p -> p.isDone() }
    }

    @Override
    List get() {
        if (initialized != null) {
            return initialized
        }
        Promises.waitAll(promises)
    }

    @Override
    List get(long timeout, TimeUnit units) throws Throwable {
        if (initialized != null) {
            return initialized
        }
        Promises.waitAll(promises, timeout, units)
    }

}
