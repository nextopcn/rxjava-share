/*
 * Copyright 2016-2017 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.rxjava.share.practices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * @author Baoyi Chen
 */
public class Practice5 {

    /*
     * example:
     * param: Observable["a","b","c"]
     * return: Single[3]
     */
    public Single<Long> count(Observable<String> source) {
    	AtomicLong i = new AtomicLong(0L);
        return source.flatMap(x -> {
        	return Observable.just(i.incrementAndGet());
        }).lastOrError();
    }

    /*
     * example:
     * param: Observable[["a", "b", "c"], ["b", "c", "d"]]
     * return: Observable["a", "b", "c","b", "c", "d"]
     */
    public Observable<String> convert(Observable<List<String>> source) {
//    	return source.flatMap(list -> Observable.fromIterable(list));
    	return source.flatMap(Observable::fromIterable);
//    	return source.flatMapIterable(x -> x);//rx java2.0
    }
    
    /*
     * example:
     * param: Observable["a", "a", "b", "b", "c"]
     * return: Observable["a", "b", "c"]
     */
    public Observable<String> distinct(Observable<String> source) {
    	List<String> list = new ArrayList<>();
    	return source.concatMap(x -> {
    		if(!list.contains(x)) {
    			list.add(x); return Observable.just(x);
    		} 
    		else return Observable.empty();
    	});
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , conditon = x > 2 and x < 5
     * return: Observable[3, 4]
     */
    public Observable<Integer> filter(Observable<Integer> source, Predicate<Integer> conditon) {
    	return source.concatMap(x -> {
    		if(conditon.test(x)) return Observable.just(x); else return Observable.empty();
    	});
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , index = 2
     * return: Maybe[3]
     */
    public Maybe<String> elementAt(Observable<String> source, int index) {
    	AtomicInteger i = new AtomicInteger(0);
        return source.flatMap(x -> {
        	if(i.getAndIncrement() == index) return Observable.just(x);
        	else { return Observable.empty(); }
        }).firstElement();
    }

    /*
     * example:
     * param: Observable["a", "b"] , count = 2
     * return: Observable["a", "b", "a", "b"]
     */
    public Observable<String> repeat(Observable<String> source, int count) {
    	return Observable.range(0, count).concatMap(x -> source);
    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> concat(List<Observable<String>> source) {
    	return Observable.fromIterable(source).concatMap(x -> x);
    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> merge(List<Observable<String>> source) {
    	return Observable.fromIterable(source).flatMap(x -> x);
    }

    /*
     * example:
     * param: Observable["a", "b", "c"], 1, SECONDS
     * return: Observable["a", "b", "c"], 每个元素都延迟1秒
     */
    public Observable<String> delayAll(Observable<String> source, long delay, TimeUnit unit) {
    	return source.concatMap(x ->Observable.just(x).delay(delay, unit));
    }
}
