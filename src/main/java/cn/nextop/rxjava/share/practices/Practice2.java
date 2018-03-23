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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Baoyi Chen
 */
public class Practice2 {

    /*
     * 举例:
     * words = Observable["a", "a", "b", "c", "c"]
     * 返回: Observable[("a", 2), ("b", 1), ("c", 2)]
     */
    public Observable<Tuple2<String, Integer>> wordCount1(Observable<String> words) {

        return Observable.create(emitter -> {
            ArrayList<Tuple2<String, Integer>> list = new ArrayList();
            words.doOnComplete(() -> {
                for (Tuple2<String, Integer> tuple2 : list) {
                    emitter.onNext(tuple2);
                }
                emitter.onComplete();
            }).subscribe(s -> {
                int value = 1;
                for (Tuple2<String, Integer> tuple2 : list) {
                    if (tuple2.getV1().equals(s)) {
                        list.remove(tuple2);
                        value = tuple2.getV2() + 1;
                        break;
                    }
                }
                list.add(new Tuple2<>(s, value));
            });

        });
    }

    /*
     * 举例:
     * words = Observable["a", "a", "b", "c", "c"]
     * 返回: Single[Map{a=2, b=1, c=2}]
     */
    public Single<Map<String, Integer>> wordCount2(Observable<String> words) {
        return Single.create(singleEmitter -> {
            HashMap<String, Integer> map = new HashMap<>();
            words.doOnComplete(() -> singleEmitter.onSuccess(map))
                    .subscribe(s -> {
                        Integer i = map.get(s);
                        map.put(s, i == null ? 1 : i++);
                    });
        });
    }

}
