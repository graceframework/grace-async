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
package org.grails.async.factory

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic

import grails.async.Promise

/**
 * A bound promise is a promise which is already resolved and doesn't require any asynchronous processing to calculate the value
 *
 * @author Graeme Rocher
 * @since 2.3
 */
@CompileStatic
class BoundPromise<T> implements Promise<T> {

    T value

    BoundPromise(T value) {
        this.value = value
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
        if (value instanceof Throwable) {
            throw value
        }
        return value
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
        if (!(value instanceof Throwable)) {
            return new BoundPromise<>((T) callable.call(value))
        }
        return this
    }

    @Override
    Promise<T> onError(Closure callable) {
        if (value instanceof Throwable) {
            return new BoundPromise<>((T) callable.call(value))
        }
        return this
    }

    @Override
    Promise<T> then(Closure callable) {
        if (!(value instanceof Throwable)) {
            try {
                final value = callable.call(value)
                return new BoundPromise(value)
            } catch (Throwable e) {
                return new BoundPromise(e)
            }
        } else {
            return this
        }
    }

    Promise<T> leftShift(Closure callable) {
        then callable
    }

}
