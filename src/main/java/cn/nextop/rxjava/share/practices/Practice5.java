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

import cn.nextop.rxjava.share.util.Lists;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.lang.reflect.Array;
import java.util.ArrayList;
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
        return source.reduce((long)0, (acc, s) -> acc+1);
    }

    /*
     * example:
     * param: Observable[["a", "b", "c"], ["b", "c", "d"]]
     * return: Observable["a", "b", "c","b", "c", "d"]
     */
    public Observable<String> convert(Observable<List<String>> source) {
        return source.map(l -> Observable.fromIterable(l)).flatMap(s->s);
    }

    /*
     * example:
     * param: Observable["a", "a", "b", "b", "c"]
     * return: Observable["a", "b", "c"]
     */
    public Observable<String> distinct(Observable<String> source) {
        return new Practice2().wordCount1(source).map(t -> t.getV1());
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , conditon = x > 2 and x < 5
     * return: Observable[3, 4]
     */
    public Observable<Integer> filter(Observable<Integer> source, Predicate<Integer> conditon) {
        return source.groupBy(n -> conditon.test(n)).buffer(2).map(arr -> arr.get(0).getKey() ? arr.get(0) : arr.get(1)).flatMap(s->s);
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , index = 2
     * return: Maybe[3]
     */
    public Maybe<String> elementAt(Observable<String> source, int index) {
        return Maybe.fromFuture(source.skip(index).take(1).toFuture());
    }

    /*
     * example:
     * param: Observable["a", "b"] , count = 2
     * return: Observable["a", "b", "a", "b"]
     */
    public Observable<String> repeat(Observable<String> source, int count) {
        Observable head = Observable.empty();
        for(int i=0;i<count;i++) {
            head = Observable.concat(head, source);
        }
        return head;
    }
    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> concat(List<Observable<String>> source) {
        List arr = new ArrayList();
        for(Observable o : source) {
            o.blockingSubscribe(s -> arr.add(s));
        }
        return Observable.fromIterable(arr);
    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> merge(List<Observable<String>> source) {
        return Observable.fromIterable(source).flatMap(s->s);
    }

    /*
     * example:
     * param: Observable["a", "b", "c"], 1, SECONDS
     * return: Observable["a", "b", "c"], 每个元素都延迟1秒
     */
    public Observable<String> delayAll(Observable<String> source, long delay, TimeUnit unit) {
        return Observable.create(emiter -> {
            source.forEach(s -> {
                Observable.just(s).delay(delay, unit).blockingSubscribe(c -> {
                    emiter.onNext(c);
                });
            });
            emiter.onComplete();
        });
    }

}
