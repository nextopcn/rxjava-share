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

import java.util.concurrent.atomic.AtomicInteger;

import cn.nextop.rxjava.share.util.type.Tuple2;
import io.reactivex.Observable;

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
//        // Wrong
//        return observable.map( e -> { return new Tuple2<>(e.charAt(0) - "a".charAt(0) + 1, e); }  );

//        // Bad, has side effect
//        AtomicInteger a = new AtomicInteger(0);
//        return observable.map( e -> { int index = a.addAndGet(1); return new Tuple2<>(index, e); }  );

        return observable.map(e -> new Tuple2<Integer, String>(1, e)).scan( (pre, cur) -> new Tuple2<Integer, String>(pre.getV1() + 1, cur.getV2()));
    }
}
