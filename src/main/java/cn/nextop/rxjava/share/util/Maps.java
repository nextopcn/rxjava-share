package cn.nextop.rxjava.share.util;

import cn.nextop.rxjava.share.util.type.Tuple2;

import java.util.HashMap;
import java.util.Map;

public class Maps {

    public static <K, V> Map<K, V> of(Tuple2<K, V>... tuples) {
        Map<K, V> map = new HashMap<>();
        for (Tuple2<K, V> tuple : tuples)
            map.put(tuple.getV1(), tuple.getV2());
        return map;
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <K, V> int size(Map<K, V> map) {
        return map == null ? 0 : map.size();
    }
}
