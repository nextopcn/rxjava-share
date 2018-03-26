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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

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
		return source.reduce(0L, (x, y) -> x + 1L);
	}

	/*
	 * example:
	 * param: Observable[["a", "b", "c"], ["b", "c", "d"]]
	 * return: Observable["a", "b", "c","b", "c", "d"]
	 */
	public Observable<String> convert(Observable<List<String>> source) {
		return source.flatMapIterable(l -> l);
	}

	/*
	 * example:
	 * param: Observable["a", "a", "b", "b", "c"]
	 * return: Observable["a", "b", "c"]
	 */
	public Observable<String> distinct(Observable<String> source) {
		return source.groupBy(s -> s).map(g -> Observable.just(g.getKey())).flatMap(og -> og);
	}

	/*
	 * example:
	 * param: Observable[1, 2, 3, 4, 5] , conditon = x > 2 and x < 5
	 * return: Observable[3, 4]
	 */
	public Observable<Integer> filter(Observable<Integer> source, Predicate<Integer> conditon) {
//		return source.map(s -> {
//			if (conditon.test(s)) {
//				return Observable.<Integer>just(s);
//			} else {
//				return Observable.<Integer>empty();
//			}
//		}).flatMap(o -> o);
		return Observable.concat(source.map(s -> {
			if (conditon.test(s)) {
				return Observable.<Integer>just(s);
			} else {
				return Observable.<Integer>empty();
			}
		}));
		
	}

	/*
	 * example:
	 * param: Observable[1, 2, 3, 4, 5] , index = 2
	 * return: Maybe[3]
	 */
	public Maybe<String> elementAt(Observable<String> source, int index) {
		return source.filter(new Index(index)).firstElement();
	}
	
	private class Index implements io.reactivex.functions.Predicate<String> {
		private int i = 0;
		private final int index;
		
		private Index(int index) {
			this.index = index;
		}
		
		@Override
		public boolean test(String t) throws Exception {
			if (i++ == index) {
				return true;
			}
			return false;
		}
	}
	
	/*
	 * example:
	 * param: Observable["a", "b"] , count = 2
	 * return: Observable["a", "b", "a", "b"]
	 */
	public Observable<String> repeat(Observable<String> source, int count) {
		List<Observable<String>> list = new ArrayList<>();
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
		return Observable.fromIterable(source).concatMapDelayError(s -> s);
	}
	
	/*
	 * example:
	 * param: Observable["a"], Observable["b"]
	 * return: Observable["a", "b"]
	 */
	public Observable<String> merge(List<Observable<String>> source) {
		return Observable.fromIterable(source).flatMap(s -> s);
	}

	/*
	 * example:
	 * param: Observable["a", "b", "c"], 1, SECONDS
	 * return: Observable["a", "b", "c"], 每个元素都延迟1秒
	 */
	public Observable<String> delayAll(Observable<String> source, long delay, TimeUnit unit) {
		return source.map(t -> {
			unit.sleep(delay);
			return t;
		});
	}

}
