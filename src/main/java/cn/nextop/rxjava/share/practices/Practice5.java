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
import io.reactivex.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
        return Single.create(singleEmitter ->
                source.map(s -> 1)
                        .scan((acc, value) -> acc + 1)
                        .lastElement()
                        .subscribe(integer -> {
                            singleEmitter.onSuccess((long) integer);
                        }));
    }

    /*
     * example:
     * param: Observable[["a", "b", "c"], ["b", "c", "d"]]
     * return: Observable["a", "b", "c","b", "c", "d"]
     */
    public Observable<String> convert(Observable<List<String>> source) {
        return Observable.create(observableEmitter ->
                source.doOnComplete(() -> observableEmitter.onComplete())
                        .subscribe(strings -> {
                            for (String s : strings) {
                                observableEmitter.onNext(s);
                            }
                        }));
    }

    /*
     * example:
     * param: Observable["a", "a", "b", "b", "c"]
     * return: Observable["a", "b", "c"]
     */
    public Observable<String> distinct(Observable<String> source) {
        return Observable.create(observableEmitter -> {
            ArrayList<String> list = new ArrayList();
            source.doOnComplete(() -> {
                for (String s : list) {
                    observableEmitter.onNext(s);
                }
                observableEmitter.onComplete();
            }).subscribe(s -> {
                if (!list.contains(s)) {
                    list.add(s);
                }
            });
        });
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , conditon = x > 2 and x < 5
     * return: Observable[3, 4]
     */
    public Observable<Integer> filter(Observable<Integer> source, Predicate<Integer> conditon) {
        return Observable.create(observableEmitter -> {
            ArrayList<Integer> list = new ArrayList();
            source.doOnComplete(() -> {
                for (Integer i : list) {
                    observableEmitter.onNext(i);
                }
                observableEmitter.onComplete();
            }).subscribe(i -> {
                if (conditon.test(i)) {
                    list.add(i);
                }
            });
        });
    }

    /*
     * example:
     * param: Observable[1, 2, 3, 4, 5] , index = 2
     * return: Maybe[3]
     */
    public Maybe<String> elementAt(Observable<String> source, int index) {
        return Maybe.create(maybeEmitter ->
                source.map(s -> new Tuple2<>(0, s))
                        .scan((acc, value) -> new Tuple2<>(acc.getV1() + 1, value.getV2()))
                        .subscribe(tuple2 -> {
                            if (tuple2.getV1() == index) {
                                maybeEmitter.onSuccess(tuple2.getV2());
                            }
                        }));
    }

    /*
     * example:
     * param: Observable["a", "b"] , count = 2
     * return: Observable["a", "b", "a", "b"]
     */
    public Observable<String> repeat(Observable<String> source, int count) {
        ArrayList<Observable<String>> list = new ArrayList();
        for (int i = 0; i < count; i++) {
            list.add(source);
        }
        return Observable.concat(list);
    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> concat(List<Observable<String>> source) {
        AtomicInteger index = new AtomicInteger();
        return Observable.create(observableEmitter ->
                    doConcat(source,index,observableEmitter));
    }

    private void doConcat(List<Observable<String>> source, AtomicInteger index, ObservableEmitter<String> emitter) {
        source.get(index.getAndIncrement())
                .doOnComplete(() -> {
                    if (index.get() >= source.size()) {
                        emitter.onComplete();
                    } else {
                        doConcat(source, index, emitter);
                    }
                }).subscribe(string -> emitter.onNext(string));

    }

    /*
     * example:
     * param: Observable["a"], Observable["b"]
     * return: Observable["a", "b"]
     */
    public Observable<String> merge(List<Observable<String>> source) {
        int size = source.size();
        AtomicInteger index = new AtomicInteger();
        return Observable.create(observableEmitter ->
                Observable.fromIterable(source)
                        .subscribe(stringObservable -> stringObservable.doOnComplete(() -> {
                            if (index.incrementAndGet() == size) {
                                observableEmitter.onComplete();
                            }
                        }).subscribe(string -> observableEmitter.onNext(string))));
    }

    /*
     * example:
     * param: Observable["a", "b", "c"], 1, SECONDS
     * return: Observable["a", "b", "c"], 每个元素都延迟1秒
     */
    public Observable<String> delayAll(Observable<String> source, long delay, TimeUnit unit) {
        return Observable.create(observableEmitter ->
                source.doOnComplete(() -> observableEmitter.onComplete())
                        .subscribe(s -> {
                            Thread.sleep(1000);
                            observableEmitter.onNext(s);
                        }));
    }

}
