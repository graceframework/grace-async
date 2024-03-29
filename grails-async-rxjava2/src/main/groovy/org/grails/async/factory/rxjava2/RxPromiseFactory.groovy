package org.grails.async.factory.rxjava2

import grails.async.Promise
import grails.async.PromiseList
import grails.async.factory.AbstractPromiseFactory
import groovy.transform.CompileStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.grails.async.factory.BoundPromise

import java.util.concurrent.TimeUnit

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
        if(closures.length == 1) {
            return new RxPromise<T>(this, closures[0], Schedulers.io())
        }
        else {
            def promiseList = new PromiseList()
            for (p in closures) {
                promiseList << p
            }
            return promiseList
        }
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises) {
        return promises.collect() { Promise<T> p -> p.get( )}
    }

    @Override
    <T> List<T> waitAll(List<Promise<T>> promises, long timeout, TimeUnit units) {
        return promises.collect() { Promise<T> p -> p.get( timeout, units ) }
    }

    @Override
    def <T> Promise<List<T>> onComplete(List<Promise<T>> promises, Closure<?> callable) {
        new RxPromise<T>(this, Observable.concat(
            promises.collect() { Promise p ->
                if(p instanceof BoundPromise) {
                    return Observable.just(((BoundPromise)p).value)
                }
                else {
                    return ((RxPromise)p).toObservable()
                }
            }
        ).toList())
         .onComplete(callable)
    }

    @Override
    <T> Promise<List<T>> onError(List<Promise<T>> promises, Closure<?> callable) {
        new RxPromise<T>(this, Observable.concat(
                promises.collect() { Promise p-> ((RxPromise)p).toObservable() }
        ).toList())
                .onError(callable)
    }
}
