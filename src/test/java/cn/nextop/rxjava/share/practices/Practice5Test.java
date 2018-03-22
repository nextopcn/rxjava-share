package cn.nextop.rxjava.share.practices;

import cn.nextop.rxjava.share.util.Lists;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Baoyi Chen
 */
public class Practice5Test {

    @Test
    public void count() {
        new Practice5().count(Observable.just("a", "b", "c")).test().assertResult(3L);
    }

    @Test
    public void convert() {
        new Practice5().convert(Observable.just(Lists.of("a", "b", "c"))).test().assertResult("a", "b", "c");
    }

    @Test
    public void distinct() {
        new Practice5().distinct(Observable.just("a", "b", "b")).test().assertResult("a", "b");
    }

    @Test
    public void filter() {
        new Practice5().filter(Observable.just(1, 2, 3, 4, 5), x -> x > 2 && x < 5).test().assertResult(3, 4);
    }

    @Test
    public void elementAt() {
        new Practice5().elementAt(Observable.just("1", "2", "3", "4", "5"), 2).test().assertResult("3");
    }

    @Test
    public void repeat() {
        new Practice5().repeat(Observable.just("0", "1"), 5).test().assertResult("0", "1", "0", "1", "0", "1", "0", "1", "0", "1");
    }

    @Test
    public void concat() {
        List<String> list = new Practice5().concat(Lists.of(Observable.just("a").delay(1, TimeUnit.SECONDS), Observable.just("c").delay(1, TimeUnit.MILLISECONDS))).subscribeOn(Schedulers.newThread()).toList().blockingGet();
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void merge() {
        List<String> list = new Practice5().merge(Lists.of(Observable.just("a").delay(1, TimeUnit.SECONDS), Observable.just("c").delay(1, TimeUnit.MILLISECONDS))).subscribeOn(Schedulers.newThread()).toList().blockingGet();
        assertEquals(2, list.size());
        assertEquals("c", list.get(0));
        assertEquals("a", list.get(1));
    }

    @Test
    public void delayAll() {
        long st = System.currentTimeMillis();
        List<String> list = new Practice5().delayAll(Observable.just("a", "b", "c"), 1, TimeUnit.SECONDS).toList().blockingGet();
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertTrue(System.currentTimeMillis() - st >= 3000);
    }
}