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

import io.reactivex.Maybe;
import io.reactivex.Observable;

/**
 * @author Baoyi Chen
 */
public class Practice3 {

    /*
     * 根据iterate的结果求和
     */
    public Maybe<Integer> sum(Observable<Node> observable) {
    	return this.iterate(observable).reduce((a,b) -> a + b);
    }

    /*
     * 举例:
     *                 5
     *                / \
     *               6   7
     *              / \   \
     *             4   3  nil
     *
     * return Observable[4, 3, 6, 7, 5] 顺序无关
     */
    public Observable<Integer> iterate(Observable<Node> observable) {
    	return observable.flatMapIterable(n -> {
    		List<Node> l = new ArrayList<>();
    		this.addLeafToList(l, n);
    		return l;
    	}).map(n -> n.value);
    }

	private void addLeafToList(List<Node> l, Node n) {
		if (n == null) {
			return;
		}
		l.add(n);
		this.addLeafToList(l, n.left);
		this.addLeafToList(l, n.right);
	}

    public static class Node {
        public Node left;
        public Node right;
        public int value;

        public Node(Node left, Node right, int value) {
            this.left = left;
            this.right = right;
            this.value = value;
        }
    }

}
