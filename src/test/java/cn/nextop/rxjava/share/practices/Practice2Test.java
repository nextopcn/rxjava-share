package cn.nextop.rxjava.share.practices;

import cn.nextop.rxjava.share.util.Tuples;
import cn.nextop.rxjava.share.util.type.Tuple2;
import io.reactivex.Observable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Baoyi Chen
 */
public class Practice2Test {

    @Test
    public void test1() {
        List<Tuple2<String, Integer>> values = new ArrayList<>();
        values.add(Tuples.of("a", 2));
        values.add(Tuples.of("b", 1));
        values.add(Tuples.of("c", 2));
        List<Tuple2<String, Integer>> list = new Practice2().wordCount1(Observable.just("a", "a", "b", "c", "c")).toList().blockingGet();
        assertEquals(3, list.size());
        for (Tuple2<String, Integer> tuple2 : list) {
            assertTrue(values.contains(tuple2));
        }
    }

    @Test
    public void test2() {
        Map<String, Integer> map = new Practice2().wordCount2(Observable.just("a", "a", "b", "c", "c")).blockingGet();
        Map<String, Integer> values = new HashMap<>();
        values.put("a", 2);
        values.put("b", 1);
        values.put("c", 2);
        assertEquals(3, map.size());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            assertTrue(values.containsKey(entry.getKey()));
        }
    }
}