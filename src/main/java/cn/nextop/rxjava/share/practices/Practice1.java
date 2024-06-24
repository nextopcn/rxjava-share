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
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.subjects.PublishSubject;

/**
 * @author Baoyi Chen
 */
public class Practice1 {

    /*
     * 举例如下:
     * 参数 Observable["a","b","c"]
     * 返回值 Observable[(1, "a"), (2, "b"), (3, "c")] 注意index从1开始
     */
    public Observable<Tuple2<Integer, String>> indexable(Observable<String> observable) {
        return  this.method3(observable);
    }

    private Observable<Tuple2<Integer, String>> method1(Observable<String> source) {
        return source.compose(new ObservableTransformer<String, Tuple2<Integer, String>>() {
            int index = 1;
            @Override
            public ObservableSource<Tuple2<Integer, String>> apply(@NonNull Observable<String> upstream) {
                return Observable.create(emitter -> {
                    upstream.subscribe(sourceItem -> {
                        emitter.onNext(new Tuple2<Integer, String>(index++, sourceItem));
                    }, e -> emitter.onError(e), () -> emitter.onComplete());
                });
            }
        });
    }

    private Observable<Tuple2<Integer, String>> method2(Observable<String> source) {
        return source.map(x -> new Tuple2<Integer, String>(1, x))
                .scan((t, s) -> new Tuple2(t.getV1().intValue() + 1, s.getV2()));
    }

    private Observable<Tuple2<Integer, String>> method3(Observable<String> source) {
//        Observable.zip
        return source.zipWith(Observable.range(1, Integer.MAX_VALUE), (o1, o2) -> {
            return new Tuple2<Integer, String>(o2, o1);
        });
    }



}
