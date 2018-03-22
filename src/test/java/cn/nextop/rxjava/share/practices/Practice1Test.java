package cn.nextop.rxjava.share.practices;

import cn.nextop.rxjava.share.util.Tuples;
import io.reactivex.Observable;
import org.junit.Test;


/**
 * @author Baoyi Chen
 */
public class Practice1Test {

    @Test
    public void test() {
        new Practice1().indexable(Observable.just("a", "b", "c")).test().assertResult(Tuples.of(1, "a"), Tuples.of(2, "b"), Tuples.of(3, "c"));
    }

}