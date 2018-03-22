## 练习中可能会用到的操作符(有多种方式实现,不限于以下操作符)

* create

* just

* empty

* zipWith

* range

* groupBy

* reduce

* concatMap

* concat

* map

* flatMap

* filter

* firstElement

* fromIterable

* delay


## 针对非java开发, 可能会用到下述语法

```java  
    Tuples.of(T1, T2) // 创建一个Tuple<T1,T2>的元组
    
    Map<K, V> map = new HashMap<>() // 创建一个字典
    
    map.containsKey(key) // 字典是否包含key
    
    map.put(key, value) // 放入字典
    
    map.get(key) //得到某个key的value
    
    Integer.MAX_VALUE //Integer类型的数值上限
    
    Long a = 5L; int b = a.intValue(); // 将非null的Long型转int
    
    List<E> subList(int fromIndex, int toIndex); // 相当于其他语言的slice
    
    Observable.empty() 不发射数据, 在filter里可以起到过滤非法数据的独特作用
    
    将Observable<T> 变成 Observable<Observable<T>> 有时候很有用
    
```

## lambda

* [Lambda-QuickStart](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/Lambda-QuickStart/index.html)