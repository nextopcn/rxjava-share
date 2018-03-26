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

import cn.nextop.rxjava.share.util.type.Tuple2;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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
//        Single<Long> single = Single.create(emitter -> {
//            Observable<Tuple2<Long, String>> ob = Observable.create( emt -> {
//                source.map(e -> { emt.onNext(new Tuple2<Long, String>(1L, e)); return e; });
//            });
//            ob.scan((last, current) -> {
//                return new Tuple2<Long, String>(last.getV1() + current.getV1(), current.getV2());
//            });
//            ob.lastElement().subscribe(e -> emitter.onSuccess(e.getV1()));
//        });
//        return single;
        return source.map( e -> new Tuple2<Long, String>(1L, e)).reduce(new Tuple2<Long, String>(0L, ""), (x, y) -> new Tuple2<Long, String>(x.getV1() + 1L, y.getV2())).map(e -> e.getV1());
    }

    /*
     * example:
     * param: Observable[["a", "b", "c"], ["b", "c", "d"]]
     * return: Observable["a", "b", "c","b", "c", "d"]
     */
    public Observable<String> convert(Observable<List<String>> source) {
            return source.map(e -> Observable.<String>create(emitter -> {
                e.forEach(item -> emitter.onNext(item));
                emitter.onComplete();
            })).concatMap(e -> e);
    }

    /*
     * example:
     * param: Observable["a", "a", "b", "b", "c"]
     * return: Observable["a", "b", "c"]
     */
    public Observable<String> distinct(Observable<String> source) {
        return source.groupBy(e -> e).map(e -> e.getKey());
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , conditon = x > 2 and x < 5
     * return: Observable[3, 4]
     */
    public Observable<Integer> filter(Observable<Integer> source, Predicate<Integer> conditon) {
        return source.map(e -> { if (conditon.test(e)) { return Observable.just(e); } else { return Observable.<Integer>empty(); } }).concatMap(e -> e);
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , index = 2
     * return: Maybe[3]
     */
    public Maybe<String> elementAt(Observable<String> source, int index) {
        return source.map(e -> new Tuple2<Integer, String>(0, e)).scan((pre, cur) -> new Tuple2<Integer, String>(pre.getV1() + 1, cur.getV2())).reduce((pre, cur) -> cur.getV1() == (Integer)index ? cur :
                pre).map(e -> e.getV2());
    }

    /*
     * example:
     * param: Observable["a", "b"] , count = 2
     * return: Observable["a", "b", "a", "b"]
     */
    public Observable<String> repeat(Observable<String> source, int count) {
        return Observable.range(1, count).concatMap(e -> source);
    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> concat(List<Observable<String>> source) {
        return Observable.fromIterable(source).concatMap(e -> e);
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
        return source.map(e -> Observable.just(e).delay(1, TimeUnit.SECONDS)).concatMap(e -> e);
    }

}
