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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Baoyi Chen
 */
public class Practice3 {

    /*
     * 根据iterate的结果求和
     */
    public Maybe<Integer> sum(Observable<Node> observable) {
        return this.iterate(observable).reduce((x, y) -> x + y);
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
        // TODO:  直接定义一个mapper function 应该更decent
        return observable.flatMapIterable(n -> {
            List<Node> r = new ArrayList<>(8); this.flat(r, n); return r;
        }).map(i -> i.value);
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

    protected void flat(List<Node> list, Node node) {
        if (node == null) return;
        list.add(node); this.flat(list, node.left); this.flat(list, node.right);
    }

}
