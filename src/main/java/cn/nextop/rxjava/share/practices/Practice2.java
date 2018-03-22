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
import io.reactivex.Single;

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
        throw new UnsupportedOperationException("implementation");
    }

    /*
     * 举例:
     * words = Observable["a", "a", "b", "c", "c"]
     * 返回: Single[Map{a=2, b=1, c=2}]
     */
    public Single<Map<String, Integer>> wordCount2(Observable<String> words) {
        throw new UnsupportedOperationException("implementation");
    }

}
