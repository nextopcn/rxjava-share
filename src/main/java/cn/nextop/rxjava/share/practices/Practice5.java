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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import cn.nextop.rxjava.share.util.Tuples;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
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
//    	return source.count();
    	return source.reduce(0L, (a, b) -> a + 1);
    }

    /*
     * example:
     * param: Observable[["a", "b", "c"], ["b", "c", "d"]]
     * return: Observable["a", "b", "c","b", "c", "d"]
     */
    public Observable<String> convert(Observable<List<String>> source) {
    	return source.flatMap(e -> Observable.fromIterable(e));
    }

    /*
     * example:
     * param: Observable["a", "a", "b", "b", "c"]
     * return: Observable["a", "b", "c"]
     */
    public Observable<String> distinct(Observable<String> source) {
//    	return source.distinct();
    	return source.groupBy(e -> e).map(e -> e.getKey());
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , conditon = x > 2 and x < 5
     * return: Observable[3, 4]
     */
    public Observable<Integer> filter(Observable<Integer> source, Predicate<Integer> conditon) {
        return source.concatMap(e -> {
            if (conditon.test(e)) return Observable.just(e); else return Observable.<Integer>empty();
        });
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , index = 2
     * return: Maybe[3]
     */
    public Maybe<String> elementAt(Observable<String> source, int index) {
//    	return source.elementAt(index);
    	return source.zipWith(Observable.range(0, Integer.MAX_VALUE), (a, b) -> Tuples.of(a, b)).filter(x -> x.getV2() == index).map(e -> e.getV1()).firstElement();
    }

    /*
     * example:
     * param: Observable["a", "b"] , count = 2
     * return: Observable["a", "b", "a", "b"]
     */
    public Observable<String> repeat(Observable<String> source, int count) {
//    	return source.repeat(count);
    	return Observable.range(0, count).concatMap(e -> source);
    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> concat(List<Observable<String>> source) {
//    	return Observable.concat(source);
    	return Observable.create(emitter -> { concat(source, emitter); });
    }
    private void concat(List<Observable<String>> source, ObservableEmitter<String> emitter) {
        if (source.isEmpty()) {
            emitter.onComplete();
        } else {
            source.get(0).subscribe(e -> {
                emitter.onNext(e);
            }, e -> emitter.onError(e), () -> {
                concat(source.subList(1, source.size()), emitter);
            });
        }
    }
    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> merge(List<Observable<String>> source) {
    	return Observable.fromIterable(source).flatMap(e -> e);
    }

    /*
     * example:
     * param: Observable["a", "b", "c"], 1, SECONDS
     * return: Observable["a", "b", "c"], 每个元素都延迟1秒
     */
    public Observable<String> delayAll(Observable<String> source, long delay, TimeUnit unit) {
//    	return source.delay(delay, unit);
    	return source.concatMap(e -> Observable.just(e).delay(delay, unit));
    }

}
