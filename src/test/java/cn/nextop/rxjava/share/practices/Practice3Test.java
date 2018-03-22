package cn.nextop.rxjava.share.practices;

import io.reactivex.Observable;
import org.junit.Test;

/**
 * @author Baoyi Chen
 */
public class Practice3Test {

    @Test
    public void test() {
        Practice3.Node n = new Practice3.Node(new Practice3.Node(new Practice3.Node(null, null, 4), null, 3), new Practice3.Node(new Practice3.Node(new Practice3.Node(null, null, 8), null, 7), null, 6), 5);
        new Practice3().sum(Observable.just(n)).test().assertResult(33);
    }
}