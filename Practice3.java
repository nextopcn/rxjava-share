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

import io.reactivex.*;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Baoyi Chen
 */
public class Practice3 {

    private Observable<Integer> nodes(Node left, Node right, Integer value) {
        if (left != null && right != null) {
            return Observable.just(left, right, new Node(null, null, value)).flatMap((Function<Node, ObservableSource<Integer>>) node1 -> nodes(node1.left, node1.right, node1.value));
        } else if (left != null) {
            return Observable.just(left, new Node(null, null, value)).flatMap((Function<Node, ObservableSource<Integer>>) node1 -> nodes(node1.left, node1.right, node1.value));
        } else if (right != null) {
            return Observable.just(right, new Node(null, null, value)).flatMap((Function<Node, ObservableSource<Integer>>) node1 -> nodes(node1.left, node1.right, node1.value));
        } else {
            return Observable.just(value);
        }
    }

    /*
     * 根据iterate的结果求和
     */
    public Maybe<Integer> sum(Observable<Node> observable) {
        return Maybe.create(maybeEmitter ->
                observable
                        .flatMap((Function<Node, ObservableSource<Integer>>) node -> nodes(node.left, node.right, node.value))
                        .scan((integer, integer2) -> integer + integer2)
                        .lastElement()
                        .subscribe(value -> maybeEmitter.onSuccess(value))
        );
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
        return Observable.create(maybeEmitter ->
                observable
                        .doOnComplete(() -> maybeEmitter.onComplete())
                        .flatMap((Function<Node, ObservableSource<Integer>>) node -> nodes(node.left, node.right, node.value))
                        .subscribe(value -> maybeEmitter.onNext(value))
        );
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
