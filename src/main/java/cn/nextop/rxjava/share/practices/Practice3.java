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

import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;


/**
 * @author Baoyi Chen
 */
public class Practice3 {

    /*
     * 根据iterate的结果求和
     */
    public Maybe<Integer> sum(Observable<Node> observable) {
        return this.iterate(observable).reduce(0, (s, i) -> s += i).toMaybe();
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
        // flatMap no map
        return observable.flatMap(s -> {
            return Observable.fromIterable(iterator(s));
        });
    }

    private Iterable<Integer> iterator(Node node) {
        class Iter implements Iterator<Integer> {
            private Stack<Node> stack = new Stack<>();
            Iter(Node node) {
                this.walk(node);
            }

            private Node walk(Node node) {
                this.stack.push(node);
                if (node.left != null) {
                    this.walk(node.left);
                }
                if (node.right != null) {
                    walk(node.right);
                }
                return null;
            }
            @Override
            public boolean hasNext() {
                return this.stack.size() > 0;
            }

            @Override
            public Integer next() {
                Node curr = this.stack.pop();
                return curr.value;
            }
        }
        return node == null ? null : () -> new Iter(node);
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
