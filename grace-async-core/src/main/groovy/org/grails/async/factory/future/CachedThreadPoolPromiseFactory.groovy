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
package org.grails.async.factory.future

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.RunnableFuture
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import jakarta.annotation.PreDestroy

import grails.async.Promise
import grails.async.PromiseList
import grails.async.factory.AbstractPromiseFactory
import org.grails.async.factory.BoundPromise

/**
 * A promise factory that uses an ExecutorService by default
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class CachedThreadPoolPromiseFactory extends AbstractPromiseFactory implements Closeable, ExecutorPromiseFactory {

    @Delegate
    final ExecutorService executorService

    CachedThreadPoolPromiseFactory(int maxPoolSize = Integer.MAX_VALUE, long timeout = 60L, TimeUnit unit = TimeUnit.SECONDS) {
        CachedThreadPoolPromiseFactory pf = this
        this.executorService = new ThreadPoolExecutor(0, maxPoolSize, timeout, unit, new SynchronousQueue<Runnable>()) {

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                return new FutureTaskPromise<T>(pf, callable)
            }

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                return new FutureTaskPromise<T>(pf, runnable, value)
            }

        }
    }

    @Override
    <T> Promise<T> createPromise(Class<T> returnType) {
        return new BoundPromise<T>(null)
    }

    @Override
    Promise<Object> createPromise() {
        return new BoundPromise<Object>(null)
    }

    @Override
    <T> Promise<T> createPromise(Closure<T>... closures) {
        if (closures.length == 1) {
            Closure<T> callable = closures[0]
            applyDecorators(callable, null)
            (Promise<T>) this.executorService.submit((Callable) callable)
        } else {
            PromiseList list = new PromiseList<>()
            for (c in closures) {
                list.add(c)
            }
            return (Promise<T>) list
        }
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises) {
        return promises.collect { Promise<T> p -> p.get() }
    }

    @Override
    def <T> List<T> waitAll(List<Promise<T>> promises, long timeout, TimeUnit units) {
        return promises.collect { Promise<T> p -> p.get(timeout, units) }
    }

    @Override
    def <T> Promise<List<T>> onComplete(List<Promise<T>> promises, Closure<?> callable) {
        return (Promise<List<T>>) this.executorService.submit((Callable) {
            while (promises.every { Promise p -> !p.isDone() }) {
                // wait
            }
            def values = promises.collect { Promise<T> p -> p.get() }
            callable.call(values)
        })
    }

    @Override
    <T> Promise<List<T>> onError(List<Promise<T>> promises, Closure<?> callable) {
        return (Promise<List<T>>) this.executorService.submit((Callable) {
            while (promises.every { Promise p -> !p.isDone() }) {
                // wait
            }
            try {
                promises.each { Promise<T> p -> p.get() }
            } catch (Throwable e) {
                callable.call(e)
                return e
            }
        })
    }

    @Override
    @PreDestroy
    void close() {
        if (!this.executorService.isShutdown()) {
            this.executorService.shutdown()
        }
    }

}
