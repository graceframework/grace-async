/*
 * Copyright 2012-2025 the original author or authors.
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
package org.grails.async.factory

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic

import grails.async.Promise

/**
 * A promise that executes synchronously, in the same thread as the creator
 *
 * @author Graeme Rocher
 * @since 2.3
 */
@CompileStatic
class SynchronousPromise<T> implements Promise<T> {

    Closure<T> callable
    def value
    boolean executed = false

    SynchronousPromise(Closure<T> callable) {
        this.callable = callable
    }

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
        return true
    }

    @Override
    T get() throws Throwable {
        if (!executed) {
            executed = true
            try {
                value = callable.call()
            } catch (e) {
                value = e
            }
        }
        if (value instanceof Throwable) {
            throw value
        }
        return (T) value
    }

    @Override
    T get(long timeout, TimeUnit units) throws Throwable {
        return get()
    }

    @Override
    Promise<T> accept(T value) {
        this.value = value
        return this
    }

    @Override
    Promise<T> onComplete(Closure callable) {
        try {
            T value = get()
            callable.call(value)
        } catch (ignore) {
        }
        return this
    }

    @Override
    Promise<T> onError(Closure callable) {
        try {
            get()
        } catch (e) {
            callable.call(e)
        }
        return this
    }

    @Override
    Promise<T> then(Closure callable) {
        T value = get()
        return new SynchronousPromise<T>(callable.curry(value))
    }

    Promise<T> leftShift(Closure callable) {
        then callable
    }

}
