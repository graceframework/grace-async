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
package org.grails.async.factory.rxjava

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers

import grails.async.Promise
import grails.async.PromiseList
import grails.async.factory.AbstractPromiseFactory
import org.grails.async.factory.BoundPromise

/**
 * An RxJava {@link grails.async.PromiseFactory} implementation
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
class RxPromiseFactory extends AbstractPromiseFactory {

    @Override
    <T> Promise<T> createPromise(Class<T> returnType) {
        return new RxPromise<T>(this, Single.just(null))
    }

    @Override
    Promise<Object> createPromise() {
        return new RxPromise<Object>(this, Single.just(null))
    }

    @Override
    <T> Promise<T> createPromise(Closure<T>[] closures) {
        if (closures.length == 1) {
            return new RxPromise<T>(this, closures[0], Schedulers.io())
        }
        def promiseList = new PromiseList()
        for (p in closures) {
            promiseList << p
        }
        return promiseList
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises) {
        return promises.collect { Promise<T> p -> p.get() }
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises, long timeout, TimeUnit units) {
        return promises.collect { Promise<T> p -> p.get(timeout, units) }
    }

    @Override
    <T> Promise<List<T>> onComplete(List<Promise<T>> promises, Closure<?> callable) {
        new RxPromise<T>(this, (Observable<List<T>>) Observable.concat(
                promises.collect { Promise p ->
                    if (p instanceof BoundPromise) {
                        return Observable.just(((BoundPromise) p).value)
                    }
                    return ((RxPromise) p).subject
                }
        ).toList())
                .onComplete(callable)
    }

    @Override
    <T> Promise<List<T>> onError(List<Promise<T>> promises, Closure<?> callable) {
        new RxPromise<T>(this, (Observable<List<T>>) Observable.concat(
                promises.collect { Promise p -> ((RxPromise) p).subject }
        ).toList())
                .onError(callable)
    }

}
