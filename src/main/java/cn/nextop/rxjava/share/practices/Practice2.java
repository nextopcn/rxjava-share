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
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import javafx.collections.ObservableList;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.security.auth.Subject;

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
//        // Passed, but not good
//        Hashtable<String, Integer> info = new Hashtable<String, Integer>();
//        words.map( e -> { Integer a = info.get(e); a = (a == null ? 1 : a+1); info.put(e, a); return e; } ).subscribe();
//        System.out.println(info.toString());
//        Observable< Tuple2<String, Integer> > ob = Observable.< Tuple2<String, Integer> >create( e -> {
//            Enumeration<String> en = info.keys();
//            while (en.hasMoreElements()) {
//                String k = en.nextElement(); Integer v = info.get(k);
//                e.onNext(new Tuple2<String, Integer>(k, v));
//            }
//            e.onComplete();
//        });
//
//        return ob;

        //AtomicInteger a = new AtomicInteger(0);
        Observable< Tuple2<String, Integer> > ob = Observable.< Tuple2<String, Integer> >create( emitter -> {
            words.groupBy( e -> e ).subscribe( e ->{ e.count().subscribe( count -> { emitter.onNext(new Tuple2<String, Integer>(e.getKey(), count.intValue())); } ); });
            emitter.onComplete();
        });

        return ob;
    }

    /*
     * 举例:
     * words = Observable["a", "a", "b", "c", "c"]
     * 返回: Single[Map{a=2, b=1, c=2}]
     */
    public Single<Map<String, Integer>> wordCount2(Observable<String> words) {
        HashMap<String, Integer> info = new HashMap<String, Integer>();
        words.groupBy( e -> e ).subscribe( e ->{ e.count().subscribe( count -> { info.put(e.getKey(), count.intValue()); } ); });

        return Single.just(info);
    }

}
