# Rxjava2介绍

## 什么是ReactiveX  

ReactiveX.io给的定义是，Rx是一个使用可观察数据流进行异步编程的编程接口，ReactiveX结合了观察者模式、迭代器模式和函数式编程的精华。  
  
## 名词定义

* Observable:　可观察对象, 在观察者模式中是被观察的对象，一旦数据产生或发生变化，会通过某种方式通知观察者或订阅者。在异步环境下，也可以叫做生产者
* Observer  :  观察者对象，监听Observable发射的数据并做出响应，Subscriber是它的一个特殊实现。在异步环境下，也可以是Observable发射的数据的消费者，在其它的文档和场景里，有时我们也将Observer叫做Subscriber、Watcher、Reactor

## 概述
  
在ReactiveX中，一个观察者(Observer)订阅一个可观察对象(Observable)。观察者对Observable发射的数据或数据序列作出响应。这种模式可以极大地简化并发操作，因为它相当于创建了一个回调，当数据准备完成时，apply这个回调。  
  
## Getting Start

* Observable示例

```java  
    
    // 创建可观察对象
    Observable<String> ob = Observable.just("1", "2", "3");
    
    // 对可观察对象进行变换，产生新的可观察对象
    Observable<Integer> ob1 = ob.map(e -> Integer.parseInt(e));
    
    // 创建观察者对象监听ob
    ob.subscribe(e-> {
        System.out.println(e + "," + e.getClass());
    });
    
    // 创建观察者对象监听ob1
    ob1.subscribe( e-> {
        System.out.println(e + "," + e.getClass());
    });
    
    // 执行结果
    // 1,class java.lang.String
    // 2,class java.lang.String
    // 3,class java.lang.String
    // 1,class java.lang.Integer
    // 2,class java.lang.Integer
    // 3,class java.lang.Integer
    
    // emit的概念
    
    Observable<Integer> ob = Observable.<Integer>create(emitter -> {
        emitter.onNext(1);
        emitter.onNext(2);
        emitter.onNext(3);
        emitter.onComplete();
    });

```

* Single与Maybe示例

```java  
    // Single也是一种Observable，区别是只emit一个值或错误
    Single<String> ob = Single.just("abc");
    
    // Maybe也是一种Observable，区别是有可能不emit值或者emit一个值或错误
    Maybe<String> maybe = Maybe.empty();

```

* Subject示例

```java  
    // Subject既是一个Observable又是一个Observer, 也可以在既有的Observable和Observer中间当作代理
    
    // Subject作为Observable的例子
    Subject<String> subject = PublishSubject.create();
    subject.subscribe(e -> System.out.println(e));
    subject.onNext("a");
    subject.onNext("b");
    subject.onNext("c");
    
    // Subject作为Observer的例子
    Subject<String> subject = PublishSubject.create(); 
    subject.subscribe(e -> System.out.println(e));     // 1
    subject.subscribe(e -> System.out.println(e));     // 2
    subject.subscribe(e -> System.out.println(e));     // 3
    
    Observable.just("1", "2", "3").subscribe(subject); //真正干活的代码在2 处
                                                       1,2,3
                                                     -----------
                                            ---------|Observer1|------  // 1
                                            |        -----------
                 1,2,3           Proxy      |          1,2,3
              ------------     ---------    |        -----------
    ----------|Observable|-----|Subject|----|--------|Observer2|------  // 2
              ------------     ---------    |        -----------
                                            |          1,2,3
                                            |        -----------
                                            ---------|Observer3|------  // 3
                                                     -----------
                                            
                                            
```

* Flowable示例

```java  
    // Flowable和Observable很像，区别就是Flowable带背压处理，Observable不带，稍后详解
    Flowable.just(1,2,3);
```

## 由观察者模式演变到rxjava2

```java  
    // 观察者模式
    public class Button {
        List<ButtonListener> listeners = new CopyOnWriteArrayList<>();

        public void addListener(ButtonListener listener) {
            this.listeners.add(listener);
        }

        public void delListener(ButtonListener listener) {
            this.listeners.remove(listener);
        }

        protected void notifyOnClick(int event) {
            for (ButtonListener listener : listeners) {
                listener.onClick(event);
            }
        }

        public void click(int event) {
            notifyOnClick(event);
        }
    }

    public interface ButtonListener {
        void onClick(int event);
    }

    public static void main(String[] args) {
        Button button = new Button();
        button.addListener(System.out::println);
        for (int i = 0; i < 10; i++) {
            button.click(i);
        }
    }

```

* 如果上述代码属于第三方库，不能随意更改源码，那么进行如下扩展使其转化成rxjava2风格

```java  
    // 对上述代码进行改造
    Button r = new Button();
    Observable<Integer> buttonObservable = Observable.create(s -> {
        ButtonListener listener = e -> s.onNext(e);
        r.addListener(listener);
        s.setCancellable(() -> r.delListener(listener));
    });
    
    // 订阅上述observable，并在适当的时机取消订阅
    Disposable d1 = buttonObservable.subscribe(System.out::println);
    Disposable d2 = null;
    for (int i = 0; i < 100; i++) {
        r.click(i);
        if (i == 50) {
            d1.dispose();
            d2 = buttonObservable.subscribe(System.out::println);
        }
    }
    if (d2 != null) d2.dispose();

```

* 如果可以随意更改源码，那么进行如下重构减少相应的代码量

```java  
    // 对上述代码进行改造
    public class Button {
        private final Subject<Integer> subject;
    
        public Button(Subject<Integer> subject) {
            this.subject = subject;
        }
    
        public void click(int event) {
            subject.onNext(event);
        }
    }
    
    // 订阅上述Button，并在适当的时机取消订阅
    Subject<Integer> subject = PublishSubject.create();
    Disposable d1 = subject.subscribe(System.out::println);
    Disposable d2 = null;
    Button r = new Button(subject);
    for (int i = 0; i < 100; i++) {
        r.click(i);
        if (i == 50) {
            d1.dispose();
            d2 = subject.subscribe(System.out::println);
        }
    }
    if (d2 != null) d2.dispose();
```

* observeOn与subscribeOn, 线程调度

```java  

    Observable<Integer> ob = Observable.create(emitter -> {
        for (int i = 0; i < 100; i++) {    // 1
            emitter.onNext(i);             // 1
        }                                  // 1
        emitter.onComplete();              // 1
    });
    
    
    Consumer<Integer> consumer = x -> {
        System.out.println(x);             // 2
    };
    
    
    ob.subscribeOn(Schedulers.io()).subscribe(consumer); //此种写法表示 1处于2处都在同一个IO线程执行
    
    
    ----------------------------------------------------main thread
            |
            |            ------------     ----------
            -------------|Observable|-----|Observer|----IO thread
                         ------------     ----------
    
    
    ob.observeOn(Schedulers.io()).subscribe(consumer); //此种写法表示 1处在主线程执行于2处在IO线程执行
    
                 ------------
    -------------|Observable|----------------------------main thread
                 ------------         |
                                      |      ----------
                                      -------|Observer|--IO thread
                                             ----------
                                             
    ob.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(consumer);//此种写法表示 1处在IO线程执行,2处在IO线程执行, 但分别属于两个IO线程
    
    
    -----------------------------------------------------main thread
           |
           |
           |      ------------
           |------|Observable|---------------------------IO thread
                  ------------      |
                                    |
                                    |
                                    |    ----------
                                    -----|Observer|------IO thread
                                         ----------

```

* 异步订阅

```java  
    Subject<Integer> subject = PublishSubject.create();
    subject.observeOn(Schedulers.io()).subscribe(e -> System.out.println("a" + e));
    subject.observeOn(Schedulers.io()).subscribe(e -> System.out.println("b" + e));
    Button r = new Button(subject);
    for (int i = 0; i < 100; i++) {
        r.click(i);
    }
    
    // 部分执行结果
    // a0
    // a1
    // a2
    // a3
    // a4
    // a5
    // a6
    // a7
    // b0
    // a8
    // b1
    // a9
    // b2
    // a10
    // b3
    // a11
    // b4
    // a12
    // b5
    
    //等同于
    protected void notifyOnClick(int event) {
        for (ButtonListener listener : listeners) {
            //提交event到这个listener所在的线程
            executors[shard(listener)].submit(() -> {
                listener.onClick(event);
            });
        }
    }
    
    
       ------------
    ---| Subject  |-------->----------------------------------------------main thread
       ------------          |      Evt1,Evt2
        Evt1,Evt2            |     -----------        
                             ------|Observer1|----------------------------IO thread 1
                             |     -----------        
                             |      Evt1,Evt2
                             |     -----------
                             ------|Observer2|----------------------------IO thread 2
                                   -----------                             
                                  
    
    // 另一种异步订阅
    Subject<Integer> subject = PublishSubject.create();
    
    Subject<Integer> subSubject = PublishSubject.create();
    subSubject.subscribe(e -> System.out.println("a" + e + "," + Thread.currentThread()));
    subSubject.subscribe(e -> System.out.println("b" + e + "," + Thread.currentThread()));

    subject.observeOn(Schedulers.io()).subscribe(subSubject);
    Button r = new Button(subject);
    for (int i = 0; i < 100; i++) {
        r.click(i);
    }
    
    // 部分结果
    // a0,Thread[RxNewThreadScheduler-1,5,main]
    // b0,Thread[RxNewThreadScheduler-1,5,main]
    // a1,Thread[RxNewThreadScheduler-1,5,main]
    // b1,Thread[RxNewThreadScheduler-1,5,main]
    // a2,Thread[RxNewThreadScheduler-1,5,main]
    // b2,Thread[RxNewThreadScheduler-1,5,main]
    // a3,Thread[RxNewThreadScheduler-1,5,main]
    // b3,Thread[RxNewThreadScheduler-1,5,main]
    // a4,Thread[RxNewThreadScheduler-1,5,main]
    // b4,Thread[RxNewThreadScheduler-1,5,main]
    // a5,Thread[RxNewThreadScheduler-1,5,main]
    // b5,Thread[RxNewThreadScheduler-1,5,main]
    // a6,Thread[RxNewThreadScheduler-1,5,main]
    // b6,Thread[RxNewThreadScheduler-1,5,main]
    
    //等同于
    protected void notifyOnClick(int event) {
        singleThreadExecutor.submit(() -> {
            for (ButtonListener listener : listeners) {
                listener.onClick(event);
            }
        });
    }
    
    //等同于如下图
    
       ------------
    ---| Subject  |-------->------------------------------------------------------------main thread
       ------------     |
        Evt1, Evt2      |  ------------          -----------        -----------
                        ---|subSubject|----------|Observer1|--------|Observer2|---------IO thread
                           ------------          -----------        -----------
                            Evt1, Evt2            Evt1, Evt2         Evt1, Evt2
                            
                            
    Observable.just(1,2,3).observeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(e -> System.out.println(e));                  
                            
    //等同于如下图
    
       ---------------
    ---| Observable  |-------->------------------------------------------------------------main thread
       ---------------     |
            1,2,3          |  
                           --------->------------------------------------------------------IO thread
                                            |
                                            |         1,2,3
                                            |       ----------
                                            --------|Observer|-----------------------------IO thread
                                                    ----------
                           
    Observable.just(1,2,3).subscribeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(e -> System.out.println(e));       
    
    //等同于如下图
    
       
    ------------>--------------------------------------------------------------------------main thread
                           |
                           |  
                           --------->------------------------------------------------------IO thread
                                            |
                                            |         1,2,3               1,2,3
                                            |       ------------       ----------
                                            --------|Observable|-------|Observer|----------IO thread
                                                    ------------       ----------
                              
```

* 将普通方法转变为Observable

```java  

    public List<String> getSomeList() {
        // 数据库耗时操作
        return Lists.of("chenby", "qutl", "wanlq");
    }
    
    // 阻塞调用
    List<String> list = getSomeList();
    // 其他操作
    ...
    ...
    
    //转变为Observable
    public Observable<List<String>> getSomeList() {
        return Observable.create(emitter -> {
            // 数据库耗时操作
            emitter.onNext(Lists.of("chenby", "qutl", "wanlq"));
            emitter.onComplete();
        });
    }
    
    // 非阻塞调用
    Observable<List<String>> ob = getSomeList();
    // 其他操作
    ...
    ...
    ob.subscribe(x -> System.out.println(x)); // 如果没应用调度器的话在此处阻塞（真正订阅的时候阻塞）
    // 把下面的注释打开的话不会阻塞之后的操作
    // ob.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(e -> System.out.println(e)); 

```

## 线程调度

前文已经介绍了observeOn和subscribeOn两个操作符，这两个操作符可以接受一个调度器参数  
下表展示了RxJava中可用的调度器种类：  
  
|调度器类型  |	效果         |  
|-----------|----------------|  
| Schedulers.computation()	| 用于计算任务，如事件循环或和回调处理，不要用于IO操作(IO操作请使用Schedulers.io())；默认线程数等于处理器的数量 | 
| Schedulers.from(executor)	| 使用指定的Executor作为调度器 | 
| Schedulers.single()	| submit到一个单独的线程执行，保证有序 | 
| Schedulers.io()	| 用于IO密集型任务，如异步阻塞IO操作，这个调度器的线程池会根据需要增长；对于普通的计算任务，请使用Schedulers.computation()；Schedulers.io()默认是一个CachedThreadScheduler | 
| Schedulers.newThread()	| 为每个任务创建一个新线程 | 
| Schedulers.trampoline()	| 当其它排队的任务完成后，在当前线程排队开始执行 | 
  
在rxjava2中，如果不指定observeOn和subscribeOn，默认操作符不在任何特定的调度器上执行，但是也有一些例外  
如下操作符在特定的调度器上执行  
  
|操作符	|调度器 |
|-------|-------|
|buffer(timespan)	| computation |
|buffer(timespan, count)	| computation| 
|buffer(timespan, timeshift)	|computation|
|debounce(timeout, unit)	|computation|
|delay(delay, unit)	|computation|
|delaySubscription(delay, unit)	|computation|
|interval	|computation|
|replay(time, unit)	|computation|
|replay(buffersize, time, unit)	|computation|
|replay(selector, time, unit)	|computation|
|replay(selector, buffersize, time, unit)	|computation|
|sample(period, unit)	|computation|
|skip(time, unit)	|computation|
|skipLast(time, unit)	|trampoline|
|skipLast(count, time, unit)	|trampoline|
|take(time, unit)	|computation|
|takeLast(time, unit)	|trampoline|
|takeLast(count, time, unit)	|trampoline|
|takeLastBuffer(time, unit)	|computation|
|takeLastBuffer(count, time, unit)	|computation|
|throttleFirst	|computation|
|throttleLast	|computation|
|throttleWithTimeout	|computation|
|timeInterval	|computation|
|timeout(timeout, timeUnit)	|computation|
|timeout(timeout, timeUnit, other)	|computation|
|timer	|computation|
|timestamp	|computation|
|window(timespan)|	computation|
|window(timespan, count)	|computation|
|window(timespan, timeshift)	|computation|
  
判断在哪个线程上调度可以通过源码上的注解  
  
```java  
    /**
     * Returns an Observable that emits windows of items it collects from the source ObservableSource. The resulting
     * ObservableSource emits connected, non-overlapping windows, each of a fixed duration as specified by the
     * {@code timespan} argument or a maximum size as specified by the {@code count} argument (whichever is
     * reached first). When the source ObservableSource completes or encounters an error, the resulting ObservableSource
     * emits the current window and propagates the notification from the source ObservableSource.
     * <p>
     * <img width="640" height="370" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/window6.png" alt="">
     * <dl>
     *  <dt><b>Scheduler:</b></dt>
     *  <dd>This version of {@code window} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param timespan
     *            the period of time each window collects items before it should be emitted and replaced with a
     *            new window
     * @param unit
     *            the unit of time that applies to the {@code timespan} argument
     * @param count
     *            the maximum size of each window before it should be emitted
     * @param restart
     *            if true, when a window reaches the capacity limit, the timer is restarted as well
     * @return an Observable that emits connected, non-overlapping windows of items from the source ObservableSource
     *         that were emitted during a fixed duration of time or when the window has reached maximum capacity
     *         (whichever occurs first)
     * @see <a href="http://reactivex.io/documentation/operators/window.html">ReactiveX operators documentation: Window</a>
     */
    @CheckReturnValue
    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    public final Observable<Observable<T>> window(long timespan, TimeUnit unit,
            long count, boolean restart) {
        return window(timespan, unit, Schedulers.computation(), count, restart);
    }

```

## Flowable 与背压（Backpressure）

### 背压

背压是指在异步场景中，被观察者发送事件速度远快于观察者的处理速度的情况下，一种通知上游的被观察者的策略

### rxjava2中背压的产生

当观察者与Flowable不在同一线程时，那么这个模型可以看作一个生产者与消费者。  
当生产者生产item较慢，而消费者消费item较快时，不会有任何问题。但反过来时，数据会积压到Flowable内部的一个buffer中，如果不做处理，会产生OOM错误。
如果指定了buffer的大小，那么到达buffer的临界值时，有相应的回调处理临界的状态。那么这就是背压的产生与处理过程。  

### rxjava2中背压的例子

```java  
    Flowable.<Integer>create(emitter -> {
        for (int i = 0; true; i++) {
            Thread.sleep(5);              // 生产速度延迟5ms
            emitter.onNext(i);
        }
    }, BackpressureStrategy.MISSING)      // 不指定背压策略，由onBackpressureXXX指定
      .onBackpressureBuffer(20)           // 指定背压的buffer为20，当buffer满会产生一个MissingBackpressureException异常在Throwable::printStackTrace打印
      .observeOn(Schedulers.newThread())  // 在另一个线程消费
      .subscribe(e -> {
        Thread.sleep(10);                 // 消费速度延迟10ms 
        System.out.println(e);
    }, Throwable::printStackTrace);
```

BackpressureStrategy:  
  
| 策略| 效果 |
|----|------|
|MISSING| 不指定策略，由后续的onBackpressureXXX指定策略|
|ERROR| 默认的buffer大小为128， 流速不均衡时发射MissingBackpressureException异常|
|BUFFER| 相当于无界buffer， 使用不当会OOM|
|DROP|默认的buffer大小为128,缓存最近的onNext事件|
|LATEST|默认的buffer大小为128,缓存区会保留最后的OnNext事件，覆盖之前缓存的OnNext事件。|
  
当策略为MISSING时，可以通过后续的onBackpressureXXX指定背压策略, 如上代码所示，可以自定义buffer的大小, 以及其他的背压操作符  

| 操作符| 效果 |
|----|------|
|onBackpressureBuffer| 指定背压buffer大小|
|onBackpressureDrop| 等同于DROP策略|
|onBackpressureLatest|等同于LATEST策略|

* 何时使用Flowable，何时使用Observable 

Observable  
  
1. 小于1000个元素: 比如很少的元素，不会引起应用的OOM.
2. GUI事件，比如鼠标移动或点击.
3. 同步的可观察对象，没有必要处理背压和流控

Flowable  
  
1. 处理10k+元素，当上游在一段时间发送的数据量过大的时候（这个量我们往往无法预计）
2. 当你从本地磁盘某个文件或者数据库读取数据时（这个数据量往往也很大）应当使用Flowable，这样下游可以根据需求自己控制一次读取多少数据
3. 以读取数据为主且有阻塞线程的可能时用Flowable，下游可以根据某种条件自己主动读取数据。

## "Cold" Observable 与 "Hot" Observable

### "Cold" Observable

```java  
    Observable<Integer> ob = Observable.create(emitter -> {
        System.out.println("subscribed");
        emitter.onNext(new Random().nextInt());
        emitter.onComplete();
    });
    
    ob.subscribe(e -> System.out.println(e));
    ob.subscribe(e -> System.out.println(e));
    
    // 结果:
    // subscribed
    // -1361531170
    // subscribed
    // 316772999
```

### "Hot" Observable

```java  
    ConnectableObservable<Integer> ob = Observable.<Integer>create(emitter -> {
        System.out.println("subscribed");
        emitter.onNext(new Random().nextInt());
        emitter.onComplete();
    }).publish();

    ob.subscribe(e -> System.out.println(e));
    ob.subscribe(e -> System.out.println(e));
    ob.connect();
    
    // 结果:
    // subscribed
    // 1129763832
    // 1129763832
    
    // 用refCount操作符将Hot Observable再转变为Cold Observable   X
    
    Observable<Integer> ob1 = ob.refCount();                   X
    ob1.subscribe(e -> System.out.println(e));                 X
    ob1.subscribe(e -> System.out.println(e));                 X

    Observable<Integer> ob = Observable.<Integer>create(emitter -> {
        Thread.sleep(1000);
        System.out.println("subscribed");
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            emitter.onNext(i);
        }
        emitter.onComplete();
    });

    Observable<Integer> ob1 = ob.publish().refCount().subscribeOn(Schedulers.io());

    ob1.subscribe(e -> System.out.println(e));
    ob1.subscribe(e -> System.out.println(e));

    // 结果
    subscribed
    0
    0
    1
    1
    2
    2
    3
    3
```

### "Hot" Observable 的等价方式

* 用Subject实现"Hot" Observable

```java  

    Observable<Integer> ob = Observable.create(emitter -> {
        System.out.println("subscribed");
        emitter.onNext(new Random().nextInt());
        emitter.onComplete();
    });

    Subject<Integer> subject = PublishSubject.create();

    subject.subscribe(e -> System.out.println(e));
    subject.subscribe(e -> System.out.println(e));

    ob.subscribe(subject);
```

* 等同于如下图

```java  
                                                    -----------
                                       ------------>| Observer|
                         Proxy         |            -----------
                      ------------     |
   Observable ------> |  Subject | --->|
                      ------------     |
                                       |            -----------
                                       ------------>| Observer|
                                                    -----------                                    
```
  
## 副作用1  

```java  

    Observable<Character> ob = Observable.range(1,3).map(x -> (char)(x+65));
    AtomicInteger index = new AtomicInteger(0);
    ob.subscribe(x -> {
        System.out.println("receive " + x + " at " + index.incrementAndGet());
    });
    
    // 结果
    // receive B at 1
    // receive C at 2
    // receive D at 3
    
    // 当订阅者唯一的时候，这个index看起来是正确的,但是增加了订阅者，这个index的结果就不正确了
    
    Observable<Character> ob = Observable.range(1,3).map(x -> (char)(x+65));
    AtomicInteger index = new AtomicInteger(0);
    ob.subscribe(x -> {
        System.out.println("receive " + x + " at " + index.incrementAndGet());
    });
    ob.subscribe(x -> {
        System.out.println("receive " + x + " at " + index.incrementAndGet());
    });
    // 结果
    // receive B at 1
    // receive C at 2
    // receive D at 3
    // receive B at 4
    // receive C at 5
    // receive D at 6
    
    // 避免副作用的一种方式
    Observable<Tuple2<Integer, Character>> ob = Observable.range(1,3).map(x -> new Tuple2<>(1, (char)(x+65))).scan((acc, value) -> {
        return new Tuple2<>(acc.t1 + 1, value.t2);
    });
    ob.subscribe(x -> {
        System.out.println("receive " + x.t2 + " at " + x.t1);
    });
    ob.subscribe(x -> {
        System.out.println("receive " + x.t2 + " at " + x.t1);
    });
    
    // 结果
    // receive B at 1
    // receive C at 2
    // receive D at 3
    // receive B at 1
    // receive C at 2
    // receive D at 3
    
```

## 副作用2

```java  
    public static class Tuple2<T1, T2> {
        public T1 t1;
        public T2 t2;

        public Tuple2(T1 t1, T2 t2) {
            this.t1 = t1;
            this.t2 = t2;
        }
    }

    Observable<Tuple2<String, String>> ob = Observable.just(new Tuple2<>("hello", "world"));   // 1
    ob.subscribe(x -> x.t2 = "bob"); //在此订阅中更改了t2的值，产生了副作用，影响其他的订阅          // 1
    ob.subscribe(e -> System.out.println(e.t1 + " " + e.t2));                                  // 1
    
    // 结果
    // hello bob
    
    Observable<Tuple2<String, String>> ob = Observable.create(emitter -> {
        emitter.onNext(new Tuple2<>("hello", "world"));
        emitter.onComplete();
    });

    ob.subscribe(x -> x.t2 = "bob"); //在此订阅中更改了t2的值，不会影响其他的订阅,为什么?
    ob.subscribe(e -> System.out.println(e.t1 + " " + e.t2));
    
    // 结果
    // hello world
    
    // 1 处代码的等价形式
    Tuple2<String, String> tuple = new Tuple2<>("hello", "world");
    Observable<Tuple2<String, String>> ob = Observable.create(emitter -> {
        emitter.onNext(tuple);
        emitter.onComplete();
    });

    ob.subscribe(x -> x.t2 = "bob");
    ob.subscribe(e -> System.out.println(e.t1 + " " + e.t2));
    
```

## 错误处理

* onError  

```java  

    Observable.just(1,2,3).concatWith(Observable.error(new Exception("error"))).subscribe(System.out::println, 
    e -> {
        // 错误处理
        e.printStackTrace();
    });
```

* 一些和错误处理相关的操作符

```java  

    OnErrorResumeNext(Observable<T>)
    
    S1--0--0--X
    S2        --0--|
    R --0--0----0--|
    
    onErrorReturn(Function<Throwable, T>)

    S1--0--0--X
    S2        1  
    R --0--0--1|
    
    onErrorReturnItem(T)

    S1--0--0--X
    S2        1  
    R --0--0--1|
```

## onComplete 与blockingXXx

```java  

    Observable.just(1, 2, 3).subscribe(System.out::println, e -> {}, () -> System.out.println("completed"));
    
    // blockingGet 示例
    List<Integer> list = Observable.<Integer>create(emitter -> {
        for(int i = 0; i < 5; i++) {
            emitter.onNext(i);
        }
        emitter.onComplete();  // 1
    }).toList().blockingGet();
    System.out.println(list);
    
    // 结果
    // [0, 1, 2, 3, 4]
    
    // 注释掉1处再执行
    // 调用阻塞在blockingGet上，有些rx的操作符需要配合onComplete事件，需要注意
```

## 响应式拉取(reactive pull)

```java  
        Flowable<Integer> ob = Flowable.create(emitter -> {
            long c;
            int j = 0;
            while ((c = emitter.requested()) != 0) {
                for (int i = 0; i < c; i++) {
                    Thread.sleep(1000);
                    emitter.onNext(j++);
                }
            }
            emitter.onComplete();
        }, BackpressureStrategy.ERROR);

        ob.subscribe(new DefaultSubscriber<Integer>() {
            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
                // request(1);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }

            protected void onStart() {
                request(1);
            }
        });
```

## 操作符简介
  
在rxjava2中， Observable, Flowable, Subject, Single, Maybe 类的静态方法都叫做操作符, 根据作用的不同，操作符也分几类

### 操作符分类

#### 创建操作
用于创建Observable的操作符  
  
|   操作符    |       效果       |
|------------|------------------|
|create | 通过调用观察者的方法从头创建一个Observable  |
|defer | 在观察者订阅之前不创建这个Observable，为每一个观察者创建一个新的Observable  |
|empty/never/error | 创建行为受限的特殊Observable |
|fromArray/fromFuture/fromIterable | 将其它的对象或数据结构转换为Observable  |
|interval | 创建一个定时发射整数序列的Observable  |
|just | 将对象或者对象集合转换为一个会发射这些对象的Observable  |
|range | 创建发射指定范围的整数序列的Observable  |
|repeat | 创建重复发射特定的数据或数据序列的Observable  |
|timer | 创建在一个指定的延迟之后发射单个数据的Observable  |
  
#### 变换操作  
这些操作符可用于对Observable发射的数据进行变换  
  
|   操作符    |       效果       |
|------------|------------------|
|buffer | 缓存，可以简单的理解为缓存，它定期从Observable收集数据到一个集合，然后把这些数据集合打包发射，而不是一次发射一个  |
|flatMap | 扁平映射，将Observable发射的数据变换为Observables集合，然后将这些Observable发射的数据平坦化的放进一个单独的Observable，可以认为是一个将嵌套的数据结构展开的过程。  |
|concatMap | 扁平映射，将Observable发射的数据变换为Observables集合，然后将这些Observable发射的数据平坦化的放进一个单独的Observable，可以认为是一个将嵌套的数据结构展开的过程。  |
|groupBy | 分组，将原来的Observable分拆为Observable集合，将原始Observable发射的数据按Key分组，每一个Observable发射一组不同的数据  |
|map | 映射，通过对序列的每一项都应用一个函数变换Observable发射的数据，实质是对序列中的每一项执行一个函数，函数的参数就是这个数据项  |
|scan | 扫描，对Observable发射的每一项数据应用一个函数，然后按顺序依次发射这些值  |
|window | 窗口，定期将来自Observable的数据分拆成一些Observable窗口，然后发射这些窗口，而不是每次发射一项。类似于buffer，但buffer发射的是数据，Window发射的是Observable，每一个Observable发射原始Observable的数据的一个子集  |
  
#### 过滤操作
这些操作符用于从Observable发射的数据中进行选择  
  
|   操作符    |       效果       |
|------------|------------------|
|debounce | 只有在空闲了一段时间后才发射数据，通俗的说，就是如果一段时间没有操作，就执行一次操作  |
|distinct | 去重，过滤掉重复数据项  |
|elementAt | 取值，取特定位置的数据项  |
|filter | 过滤，过滤掉没有通过谓词测试的数据项，只发射通过测试的  |
|first | 首项，只发射满足条件的第一条数据  |
|ignoreElements | 忽略所有的数据，只保留终止通知(onError或onComplete)  |
|last | 末项，只发射最后一条数据  |
|sample | 取样，定期发射最新的数据，等于是数据抽样，有的实现里叫throttleFirst  |
|skip | 跳过前面的若干项数据  |
|skipLast | 跳过后面的若干项数据  |
|take | 只保留前面的若干项数据  |
|takeLast | 只保留后面的若干项数据  |
  
#### 组合操作  
组合操作符用于将多个Observable组合成一个单一的Observable  
  
|   操作符    |       效果       |
|------------|------------------|
|combineLatest | 当两个Observables中的任何一个发射了一个数据时，通过一个指定的函数组合每个Observable发射的最新数据（一共两个数据），然后发射这个函数的结果  |
|join | 无论何时，如果一个Observable发射了一个数据项，只要在另一个Observable发射的数据项定义的时间窗口内，就将两个Observable发射的数据合并发射  |
|merge | 将两个Observable发射的数据组合并成一个  |
|startWith | 在发射原来的Observable的数据序列之前，先发射一个指定的数据序列或数据项  |
|zipWith | 打包，使用一个指定的函数将多个Observable发射的数据组合在一起，然后将这个函数的结果作为单项数据发射  |
  
#### 错误处理
这些操作符用于从错误通知中恢复  
  
|   操作符    |       效果       |
|------------|------------------|
|onErrorReturn |  |
|onErrorResumeNext |  |
|onErrorResumeItem |  |
|onErrorReturnItem |   |
|retry |   |
|retryUntil |   |
|retryWhen |   |
  
#### 辅助操作
一组用于处理Observable的操作符  
  
|   操作符    |       效果       |
|------------|------------------|
|delay | 延迟一段时间发射结果数据  |
|doAfterNext/doAfterTerminate/doFinally/doOnXxx | 注册一个回调到Observable的生命周期事件  |
|observeOn | 指定观察者调度程序上执行  |
|serialize | 强制Observable按次序发射数据  |
|subscribe | 收到Observable发射的数据和通知后执行的操作  |
|subscribeOn | 指定Observable应该在哪个调度程序上执行  |
|timeInterval | 将一个Observable转换为发射两个数据之间所耗费时间的Observable  |
|timeout | 添加超时机制，如果过了指定的一段时间没有发射数据，就发射一个错误通知  |
|timestamp | 给Observable发射的每个数据项添加一个时间戳  |
|using | 创建一个只在Observable的生命周期内存在的一次性资源  |
  
#### 条件和布尔操作  
这些操作符可用于单个或多个数据项，也可用于Observable  
  
|   操作符    |       效果       |
|------------|------------------|
|all | 判断Observable发射的所有的数据项是否都满足某个条件  |
|any | 判断Observable发射的某一个数据项是否都满足某个条件  |
|ambWith | 给定多个Observable，只让第一个发射数据的Observable发射全部数据  |
|contains | 判断Observable是否会发射一个指定的数据项  |
|defaultIfEmpty | 发射来自原始Observable的数据，如果原始Observable没有发射数据，就发射一个默认数据  |
|sequenceEqual | 判断两个Observable是否按相同的数据序列  |
|skipUntil | 丢弃原始Observable发射的数据，直到第二个Observable发射了一个数据，然后发射原始Observable的剩余数据  |
|skipWhile | 丢弃原始Observable发射的数据，直到一个特定的条件为假，然后发射原始Observable剩余的数据  |
|takeUntil | 发射来自原始Observable的数据，直到第二个Observable发射了一个数据或一个通知  |
|takeWhile | 发射原始Observable的数据，直到一个特定的条件为真，然后跳过剩余的数据  |
  
#### 算术和聚合操作
这些操作符可用于整个数据序列  
  
|   操作符    |       效果       |
|------------|------------------|
|concat | 不交错的连接多个Observable的数据  |
|count | 计算Observable发射的数据个数，然后发射这个结果  |
|reduce | 按顺序对数据序列的每一个应用某个函数，然后返回这个值  |
  
#### 连接操作
一些有精确可控的订阅行为的特殊Observable  
  
|   操作符    |       效果       |
|------------|------------------|
|connect | 指示一个可连接的Observable开始发射数据给订阅者  |
|publish | 将一个普通的Observable转换为可连接的  |
|refCount | 使一个可连接的Observable表现得像一个普通的Observable  |
|replay | 确保所有的观察者收到同样的数据序列，即使他们在Observable开始发射数据之后才订阅  |
  
#### 转换操作
  
|   操作符    |       效果       |
|------------|------------------|
|toList/toXxx | 将Observable转换为其它的对象或数据结构  |
|blockingGet/blockingWait | 阻塞Observable的操作符  |
  
#### 操作符决策树
几种主要的需求  
  
直接创建一个Observable（创建操作）  
组合多个Observable（组合操作）  
对Observable发射的数据执行变换操作（变换操作）  
从Observable发射的数据中取特定的值（过滤操作）  
转发Observable的部分值（条件/布尔/过滤操作）  
对Observable发射的数据序列求值（算术/聚合操作）  

## 创建操作符

### just,defer,create区别

```java  
    // just
    Observable<Long> ob = Observable.just(System.currentTimeMillis());
    ob.subscribe(System.out::println);
    ob.subscribe(System.out::println);
    
    // result
    // 1521432048686
    // 1521432048686
    
    // 等同于
    long ms = System.currentTimeMillis();
    Observable<Long> ob = Observable.<Long>create(emitter -> {
        emitter.onNext(ms);
        emitter.onComplete();
    });
    
    //defer
    Observable<Long> ob = Observable.defer(() -> Observable.just(System.currentTimeMillis()));
    ob.subscribe(System.out::println); // 相当于在此处执行Observable.just(System.currentTimeMillis()).subscribe(System.out::println);
    ob.subscribe(System.out::println); // 相当于在此处执行Observable.just(System.currentTimeMillis()).subscribe(System.out::println);
    // result
    // 1521432140203
    // 1521432140207
    
    //defer与create区别
    Observable<Long> ob = Observable.<Long>create(emitter -> {
        emitter.onNext(System.currentTimeMillis());
        emitter.onComplete();
    });
    
    ob.subscribe(System.out::println);
    ob.subscribe(System.out::println);
    
    // result
    // 1521432140203
    // 1521432140207
```

### interval, timer区别

```java  
    // interval
    Observable.interval(1, TimeUnit.SECONDS).subscribe(System.out::println); // 从0L开始生成Long型序列，也就是说类型是Observable<Long>
   
    // result
    // 0
    // 1
    // 2
    // ...
    
    //timer
    Observable.timer(1, TimeUnit.SECONDS).subscribe(System.out::println); // 延迟1秒返回0L，也就是说类型是Observable<Long>,并只返回0L
    
    // result
    // 0
```

## 变换操作符

* buffer

```java  

    Observable.just(1,2,3,4,5).buffer(2).subscribe(e -> System.out.println(e));
    
    // Observable[1,2,3,4,5]转换为Observable[List[1,2], List[3,4], List[5]]
    
    Observable.just(1,2,3,4).buffer(1, TimeUnit.SECONDS).subscribe(e-> System.out.println(e)); //buffer可以按时间打包数据

```

* flatMap与concatMap

```java  
    Observable<Integer> ob = Observable.just(Observable.just(1), Observable.just(2)).flatMap(e -> e); // Observable<Observable<Integer>> -> Observable<Integer>
    Observable<Integer> ob = Observable.just(Observable.just(1), Observable.just(2)).concatMap(e -> e); // Observable<Observable<Integer>> -> Observable<Integer>
    
    //Observable[Observable[1],Observable[2]]转换为Observable[1,2]，相当于解除嵌套
    //flatMap与concatMap有顺序区别，如下所示
    Observable<String> a = Observable.just("a").delay(1, TimeUnit.SECONDS);
    Observable<String> b = Observable.just("c").delay(1, TimeUnit.MILLISECONDS);
    
    Observable.just(a,b).flatMap(e -> e).subscribe(e -> System.out.println(e));
    // 结果
    // [c a]
    
    Observable.just(a,b).concatMap(e -> e).subscribe(e -> System.out.println(e));
    // 结果
    // [a c]
    
    merge与concat和这两个操作符的区别类似。
```

* map

```java  
    
    Observable<String> ob = Observable.just(1,2,3).map(e -> String.valueOf(e));
    
    // Observable[1,2,3]转换为Observable["1", "2", "3"],相当于对每一项apply一个函数
    
```

* scan

```java  
    
    Observable.just(1, 2, 3).scan((x, y) -> x + y).subscribe(e -> System.out.println(e));
    
    // Observable[1,2,3] 
    // 从第二项2开始，与前一项apply一个BiFunction
    // 本例，第一项1不变 ,第二项变为1 + 2 = 3, 第三项3 + 3 = 6
    // 结果 [1,3,6]
    
```

* groupBy

```java  
    Observable.just(Tuples.of(1, "a"), Tuples.of(1, "b"), Tuples.of(2, "d"), Tuples.of(1, "c"), Tuples.of(2, "e")).groupBy(e -> e.getV1())

    // Observable[(1,"a"),(1,"b"),(2,"d"),(1,"c"),(2,"e"]转换为Observable[[key=1, value=Observable[(1,"a"),(1,"b"),(1,"c")]],[key=2, value=Observable[(2,"d"),(2,"e")]]]
```

## 过滤操作符

* filter,distinct,elementAt,skip,first,last等操作符如字面意思，不多加解释

* take

```java  
    Observable<Integer> ob = Observable.create(emitter -> {
        AtomicBoolean flag = new AtomicBoolean(true);
        emitter.setCancellable(() -> flag.set(false));
        while (flag.get()) {
            System.out.println("onNext");
            emitter.onNext(1);
        }
    });

    ob.take(3).subscribe(e -> System.out.println(e));
```

* [debounce](http://reactivex.io/documentation/operators/debounce.html)操作符

* sample操作符

```java  

    Observable.interval(300, TimeUnit.MILLISECONDS).sample(1,TimeUnit.SECONDS).subscribe(e -> System.out.println(e));
    
    // 每300毫秒发射一个数据，如下所示
    
    // timeline  300      600      900      1200      1500     1800     2100   .....
    // data      0         1        2         3         4        5        6    .....
    // sample                           2                            5         ..... ,每1秒的最后一条记录，不是随机sample

```

## 组合操作

* [zipWith](http://reactivex.io/documentation/operators/zip.html)操作符

* merge与concat操作符参见flatMap与concatMap里的讲解

## 辅助操作

* delay

```java  
    Observable.just(1).delay(1, TimeUnit.SECONDS)// 将1延迟1秒发射
```

* timeInterval 

```java  
    Observable.interval(100, TimeUnit.MILLISECONDS).timeInterval().subscribe(e -> System.out.println(e));
    // 将一个Observable转换为发射两个数据之间所耗费时间的Observable
    
    // Observable[0,1,2,3,4,5...]
    // Observable[Timed[time=101, unit=MILLISECONDS, value=0], Timed[time=101, unit=MILLISECONDS, value=1]...]
    
```

* timeout

```java  
    Observable.interval(1000, TimeUnit.MILLISECONDS).timeout(500, TimeUnit.MILLISECONDS).subscribe(e-> System.out.println(e));
    // 添加超时机制，如果过了指定的一段时间没有发射数据，就发射一个错误通知
    // 本例抛出一个超时异常
```

* timestamp
 
```java  
    Observable.interval(1000, TimeUnit.MILLISECONDS).timestamp().subscribe(e-> System.out.println(e));
    // 给Observable发射的每个数据项添加一个时间戳 
    // Observable[0,1,2,3,4,5...]
    // Observable[Timed[time=1521443650280, unit=MILLISECONDS, value=0], Timed[time=1521443651280, unit=MILLISECONDS, value=1]...]

```
 
## 条件和布尔操作  

* ambWith
```java  

    Observable.just("a").delay(1, TimeUnit.SECONDS)
    .ambWith(Observable.just("b").delay(2, TimeUnit.SECONDS)).subscribe(e-> System.out.println(e));
    
    // Observable[a] ambwith Observable[b]
    // 返回Observable[a]
```

## 算术和聚合操作

* reduce

```java  
    Observable.just(1,2,3,4,5).reduce((x, y) -> x + 1).subscribe(e-> System.out.println(e));
    
    // Observable[1,2,3,4,5] 聚合操作， 相当于实现了count操作符
```

## 自定义操作符

* lift

```java  
    // lift操作符对Observer对象apply一个变换函数
    
    // 用lift实现map操作符
    
    Observable.just(1,2,3).lift(new ObservableOperator<String, Integer>() {

        @Override
        public Observer<? super Integer> apply(Observer<? super String> observer) throws Exception {
            return new Observer<Integer>() {

                @Override
                public void onSubscribe(Disposable d) {
                    observer.onSubscribe(d);
                }

                @Override
                public void onNext(Integer integer) {
                    observer.onNext(String.valueOf(integer) + "str");
                }

                @Override
                public void onError(Throwable e) {
                    observer.onError(e);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            };
        }
    }).subscribe(e -> System.out.println(e));
    
    // 原理
    
    lift function : Observer[String] => Observer[Integer]
    
                                        
         ---------------------          ------      ------------------
    -----|Observable<Integer>|----------|LIFT|------|Observer[String]|
         ---------------------          ------      ------------------
         
         
         ---------------------          -------------------      ------------------
    -----|Observable<Integer>|----------|Observer[Integer]|------|Observer[String]|
         ---------------------          -------------------      ------------------

```

* compose

```java  
    // compose操作符对Observable对象apply一个变换函数
    
    Observable.just(1,2,3).compose(new ObservableTransformer<Integer, String>() {

        @Override
        public ObservableSource<String> apply(Observable<Integer> upstream) {
            return Observable.create(emitter -> {
                upstream.subscribe(e -> {
                    emitter.onNext(String.valueOf(e) + "str");
                }, e -> emitter.onError(e), () -> emitter.onComplete());
            });
        }
    }).subscribe(e -> System.out.println(e));

```

## Promise

* toFuture

```java  

    Future<List<Integer>> future = Observable.just(1,2,3).toList().delay(1, TimeUnit.SECONDS).observeOn(Schedulers.io()).toFuture();
    List<Integer> list = future.get();
    System.out.println(list);

```

* fromFuture

```java  

    Future<List<Integer>> future = Observable.just(1, 2, 3).toList().delay(1, TimeUnit.SECONDS).observeOn(Schedulers.io()).toFuture();
    Observable.fromFuture(future).subscribe(e -> System.out.println(e));
    
```

# 引申阅读

* [RxJava 的 Subject](https://www.jianshu.com/p/99bd603881bf)
* [Reactive pull](http://blog.chengyunfeng.com/?p=981)

# 相关引用

* [rx doc](http://reactivex.io/documentation)
* [rx doc-cn](https://mcxiaoke.gitbooks.io/rxdocs/content/)
* [introduction rx](http://www.introtorx.com/Content/v1.0.10621.0/01_WhyRx.html)
* [cold and hot observable](https://github.com/ReactiveX/rxjs/issues/2604)
* [backpressure](https://github.com/ReactiveX/RxJava/wiki/Backpressure-(2.0))
* [RxJava操作符（十）自定义操作符](http://mushuichuan.com/2016/02/05/rxjava-operator-10/)