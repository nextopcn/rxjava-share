package cn.nextop.rxjava.share.practices;

import cn.nextop.rxjava.share.util.Lists;
import io.reactivex.Observable;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Baoyi Chen
 */
public class Practice3Test {

    @Test
    public void test() {
        Practice3.Node n = new Practice3.Node(new Practice3.Node(new Practice3.Node(null, null, 4), null, 3), new Practice3.Node(new Practice3.Node(new Practice3.Node(null, null, 8), null, 7), null, 6), 5);
        new Practice3().sum(Observable.just(n)).test().assertResult(33);
    }

    @Test
    public void test1() {
        List<Integer> list = Lists.of(4,3,5,6,7,8);
        Practice3.Node n = new Practice3.Node(new Practice3.Node(new Practice3.Node(null, null, 4), null, 3), new Practice3.Node(new Practice3.Node(new Practice3.Node(null, null, 8), null, 7), null, 6), 5);
        List<Integer> list1 = new Practice3().iterate(Observable.just(n)).toList().blockingGet();
        assertEquals(list.size(), list1.size());
        for (Integer i : list) {
            assertTrue(list1.contains(i));
        }
    }
}