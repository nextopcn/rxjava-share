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
        throw new UnsupportedOperationException("implementation");
    }
}
