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

package cn.nextop.rxjava.share.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leon Chen
 * @since 1.0.0
 */
public class Lists {

    public static <T> List<T> of(T... args) {
        List<T> list = new ArrayList<>();
        for (T t : args) list.add(t);
        return list;
    }

    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> int size(List<T> list) {
        return list == null ? 0 : list.size();
    }

    public static <T> List<T> slice(List<T> raw, int lo, int hi) {
        List<T> list = new ArrayList<>(hi - lo);
        for (int i = lo; i < hi; i++)
            list.add(raw.get(i));
        return list;
    }
}
