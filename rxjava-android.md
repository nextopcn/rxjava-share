1， Android怎样集成 RxJava2.x ? (continued support for Java 6+ & Android 2.3+ (Java 8 lambda-friendly API))

	dependencies {
		compile 'io.reactivex.rxjava2:rxjava:2.1.10'  ( 依赖 reactive-streams:1.0.2)
		compile 'io.reactivex.rxjava2:rxandroid:2.0.2'
	}

	1.1 讲解 io.reactivex.rxjava2:rxandroid:2.0.2'
		Observable.just(1).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe();
		
		AndroidSchedulers 源码
		public final class AndroidSchedulers {
		    private static final class MainHolder {
		        static final Scheduler DEFAULT = new HandlerScheduler(new Handler(Looper.getMainLooper()));
		    }

		    private static final Scheduler MAIN_THREAD = RxAndroidPlugins.initMainThreadScheduler(
		            new Callable<Scheduler>() {
		                @Override public Scheduler call() throws Exception {
		                    return MainHolder.DEFAULT;
		                }
		            });

		    /** A {@link Scheduler} which executes actions on the Android main thread. */
		    public static Scheduler mainThread() {
		        return RxAndroidPlugins.onMainThreadScheduler(MAIN_THREAD);
		    }

		    /** A {@link Scheduler} which executes actions on {@code looper}. */
		    public static Scheduler from(Looper looper) {
		        if (looper == null) throw new NullPointerException("looper == null");
		        return new HandlerScheduler(new Handler(looper));
		    }

		    private AndroidSchedulers() {
		        throw new AssertionError("No instances.");
		    }
		}

	1.2 基于 RxJava 的 Android有哪些？（Retrofit2）
		RxLifecycle - 使用RxJava生命周期处理Android应用程序的API	***
		RxBinding - 用于Android的UI小部件的RxJava绑定API。	***
		SqlBrite - SQLiteOpenHelper和ContentResolver的轻量级包装器，它将反应流语义引入到查询中。
		Android-ReactiveLocation - 使用响应式友好API封装位置播放服务API样板的库。（RxJava 1）
		RxLocation - 适用于Android的反应式位置API库。（RxJava 2）
		rx-preferences - 适用于Android的反应性SharedPreferences
		RxFit - 适用于Android的反应性健身API库
		RxWear - 适用于Android的反应式可穿戴API库
		RxPermissions - 由RxJava支持的Android运行时权限	***
		RxNotification - 使用RxJava注册，删除和管理通知的简单方法
		RxClipboard - 适用于Android剪贴板的RxJava绑定API。
		RxBroadcast - 用于Broadcast和的RxJava绑定LocalBroadcast。
		RxBroadcastReceiver - 针对Android BroadcastReceiver的简单RxJava2绑定
		RxAndroidBle - 用于处理蓝牙LE设备的反应性库。
		RxImagePicker - 用于从图库或照相机中选择图像的反应性库。
		ReactiveNetwork - 反应式库监听网络连接状态和Internet连接（与RxJava1.x和RxJava2.x兼容）
		ReactiveBeacons - 反应式库扫描附近的BLE（蓝牙低能耗）信标（兼容RxJava1.x和RxJava2.x）
		ReactiveAirplaneMode - 反应式库监听飞行模式（与RxJava2.x兼容）
		ReactiveSensors - 带RxJava的反应式库监控设备硬件传感器（与RxJava1.x和RxJava2.x兼容）
		RxDataBinding - 适用于Android数据绑定库的RxJava2绑定API。
		RxLocationManager - RxJava / RxJava2环绕标准Android LocationManager，无需Google Play服务。
		RxDownloader - 用于下载文件的活动库（与RxJava2.x兼容）。


2，RxLifecycle - 使用RxJava生命周期处理Android应用程序的API

		dependencies {
		compile 'com.trello.rxlifecycle2:rxlifecycle:2.2.1' 
		compile 'com.trello.rxlifecycle2:rxlifecycle-android:2.2.1' 
		compile 'com.trello.rxlifecycle2:rxlifecycle-components:2.2.1' 
		compile 'com.trello.rxlifecycle2:rxlifecycle-components-preference:2.2.1' 
		compile 'com.trello.rxlifecycle2:rxlifecycle-navi:2.2.1'
		compile 'com.trello.rxlifecycle2:rxlifecycle-android-lifecycle:2.2.1'
		compile 'com.trello.rxlifecycle2:rxlifecycle-kotlin:2.2.1'
		compile 'com.trello.rxlifecycle2:rxlifecycle-android-lifecycle-kotlin:2.2.1'
		}

	
	2.1 问题的由来 (OOM，如果该工作线程还没执行结束就退出Activity或者Fragment，就会无法释放引起内存泄漏)
		@Override protected void onStart() {
		    super.onStart(); System.out.println("onStart()");
		    Disposable disposable = Observable.interval(1, TimeUnit.SECONDS)
		            .subscribeOn(Schedulers.io())
		            .observeOn(AndroidSchedulers.mainThread())
		            .subscribe(v -> System.out.println(v));
		}
		protected void onDestroy() {  super.onDestroy(); disposable.dispose(); } 手动释放

		官方
		This library allows one to automatically complete sequences based on a second lifecycle stream.
		This capability is useful in Android, where incomplete subscriptions can cause memory leaks.
		这个库允许基于第二个生命周期流自动完成序列。这种功能在Android中很有用，因为不完整的订阅会导致内存泄漏。

	
	2.2 例子
	public class MainActivity extends RxAppCompatActivity {

		@Override protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState); System.out.println("onCreate()");
		    setContentView(R.layout.activity_main);
		}
		
		@Override protected void onStart() {
		    super.onStart(); System.out.println("onStart()");
		    Observable.interval(1, TimeUnit.SECONDS)
		            .subscribeOn(Schedulers.io())
		            .observeOn(AndroidSchedulers.mainThread())
		            .compose(this.<Long>bindToLifecycle())
		            //.compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE)) 
		            .subscribe(v -> System.out.println(v));
		}
		
		@Override protected void onResume() { super.onResume(); System.out.println("onResume()"); }
		@Override protected void onPause() { super.onPause(); System.out.println("onPause()"); }
		@Override protected void onStop() { super.onStop(); System.out.println("onStop()"); }
		@Override protected void onDestroy() { super.onDestroy();System.out.println("onDestroy()"); }
		@Override protected void onRestart() { super.onRestart(); System.out.println("onRestart()"); }
	}
	

    bindToLifecycle() 映射那些生命周期
    private static final Function<ActivityEvent, ActivityEvent> ACTIVITY_LIFECYCLE =
        new Function<ActivityEvent, ActivityEvent>() {
            @Override
            public ActivityEvent apply(ActivityEvent lastEvent) throws Exception {
                switch (lastEvent) {
                    case CREATE:
                        return ActivityEvent.DESTROY;
                    case START:
                        return ActivityEvent.STOP;
                    case RESUME:
                        return ActivityEvent.PAUSE;
                    case PAUSE:
                        return ActivityEvent.STOP;
                    case STOP:
                        return ActivityEvent.DESTROY;
                    case DESTROY:
                        throw new OutsideLifecycleException("Cannot bind to Activity lifecycle when outside of it.");
                    default:
                        throw new UnsupportedOperationException("Binding to " + lastEvent + " not yet implemented");
                }
            }
        };


	RxAppCompatActivity 源码
	public abstract class RxAppCompatActivity extends AppCompatActivity implements LifecycleProvider<ActivityEvent> {
		private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();
		
		@Override
		@NonNull
		@CheckResult
		public final Observable<ActivityEvent> lifecycle() {
		    return lifecycleSubject.hide();
		}
		
		@Override
		@NonNull
		@CheckResult
		public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
		    return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
		}
		
		@Override
		@NonNull
		@CheckResult
		public final <T> LifecycleTransformer<T> bindToLifecycle() {
		    return RxLifecycleAndroid.bindActivity(lifecycleSubject);
		}
		
		@Override
		@CallSuper
		protected void onCreate(@Nullable Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    lifecycleSubject.onNext(ActivityEvent.CREATE);
		}
		
		@Override
		@CallSuper
		protected void onStart() {
		    super.onStart();
		    lifecycleSubject.onNext(ActivityEvent.START);
		}
		
		@Override
		@CallSuper
		protected void onResume() {
		    super.onResume();
		    lifecycleSubject.onNext(ActivityEvent.RESUME);
		}
		
		@Override
		@CallSuper
		protected void onPause() {
		    lifecycleSubject.onNext(ActivityEvent.PAUSE);
		    super.onPause();
		}
		
		@Override
		@CallSuper
		protected void onStop() {
		    lifecycleSubject.onNext(ActivityEvent.STOP);
		    super.onStop();
		}
		
		@Override
		@CallSuper
		protected void onDestroy() {
		    lifecycleSubject.onNext(ActivityEvent.DESTROY);
		    super.onDestroy();
		}
	}

	rxlifecycle-navi 源码
	public abstract class NaviActivity extends Activity implements NaviComponent {
	
	  private final NaviEmitter base = NaviEmitter.createActivityEmitter();
	
	  @Override public final boolean handlesEvents(Event... events) {
	    return base.handlesEvents(events);
	  }
	
	  @Override public final <T> void addListener(@NonNull Event<T> event, @NonNull Listener<T> listener) {
	    base.addListener(event, listener);
	  }
	
	  @Override public final <T> void removeListener(@NonNull Listener<T> listener) {
	    base.removeListener(listener);
	  }
	
	  @Override @CallSuper protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    base.onCreate(savedInstanceState);
	  }
	
	  @Override @CallSuper public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
	    super.onCreate(savedInstanceState, persistentState);
	    base.onCreate(savedInstanceState, persistentState);
	  }
	
	  @Override @CallSuper protected void onStart() {
	    super.onStart();
	    base.onStart();
	  }
	
	  @Override @CallSuper protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    base.onPostCreate(savedInstanceState);
	  }
	
	  @Override @CallSuper public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
	    super.onPostCreate(savedInstanceState, persistentState);
	    base.onPostCreate(savedInstanceState, persistentState);
	  }
	
	  @Override @CallSuper protected void onResume() {
	    super.onResume();
	    base.onResume();
	  }
	
	  @Override @CallSuper protected void onPause() {
	    base.onPause();
	    super.onPause();
	  }
	
	  @Override @CallSuper protected void onStop() {
	    base.onStop();
	    super.onStop();
	  }
	
	  @Override @CallSuper protected void onDestroy() {
	    base.onDestroy();
	    super.onDestroy();
	  }
	
	  @Override @CallSuper protected void onRestart() {
	    super.onRestart();
	    base.onRestart();
	  }
	
	  @Override @CallSuper protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    base.onSaveInstanceState(outState);
	  }
	
	  @Override @CallSuper public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
	    super.onSaveInstanceState(outState, outPersistentState);
	    base.onSaveInstanceState(outState, outPersistentState);
	  }
	
	  @Override @CallSuper protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    base.onRestoreInstanceState(savedInstanceState);
	  }
	
	  @Override @CallSuper public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
	    super.onRestoreInstanceState(savedInstanceState, persistentState);
	    base.onRestoreInstanceState(savedInstanceState, persistentState);
	  }
	
	  @Override @CallSuper protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    base.onNewIntent(intent);
	  }
	
	  @Override @CallSuper public void onBackPressed() {
	    super.onBackPressed();
	    base.onBackPressed();
	  }
	
	  @Override @CallSuper public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    base.onAttachedToWindow();
	  }
	
	  @Override @CallSuper public void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
	    base.onDetachedFromWindow();
	  }
	
	  @Override @CallSuper public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    base.onConfigurationChanged(newConfig);
	  }
	
	  @Override @CallSuper protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    base.onActivityResult(requestCode, resultCode, data);
	  }
	
	  @Override @CallSuper public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	      @NonNull int[] grantResults) {
	    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	    base.onRequestPermissionsResult(requestCode, permissions, grantResults);
	  }
	}
	
	
3, RxBinding - 用于Android的UI小部件的RxJava绑定API。

	dependencies {
		compile 'com.jakewharton.rxbinding2:rxbinding:2.1.1'
		compile 'com.jakewharton.rxbinding2:rxbinding-support-v4:2.1.1'
		compile 'com.jakewharton.rxbinding2:rxbinding-appcompat-v7:2.1.1'
		compile 'com.jakewharton.rxbinding2:rxbinding-design:2.1.1'
		compile 'com.jakewharton.rxbinding2:rxbinding-recyclerview-v7:2.1.1'
		compile 'com.jakewharton.rxbinding2:rxbinding-leanback-v17:2.1.1'
	}
	
	例子1：Button 防抖处理
		Button btnOk = (Button) findViewById( R.id.btnOk ) ;
		RxView.clicks(btnOk)
		        //.throttleFirst( 2 , TimeUnit.SECONDS ) 
		        .subscribe(v -> Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show());
		        
	例子2：按钮的长按时间监听
		RxView.longClicks(btnOk)
		        .subscribe(v -> Toast.makeText(MainActivity.this, "long click", Toast.LENGTH_SHORT).show()) ;
		        
	例子3：listView 的点击事件、长按事件处理
		RxAdapterView.itemClicks( listView )
		     .subscribe() ;
		
		RxAdapterView.itemLongClicks( listView)
		     .subscribe() ;

	  
	  实现类
		final class ViewClickObservable extends Observable<Object> {
		private final View view;
		
		ViewClickObservable(View view) {
		    this.view = view;
		}
		
		@Override protected void subscribeActual(Observer<? super Object> observer) {
		    if (!checkMainThread(observer)) {
		      return;
		    }
		    Listener listener = new Listener(view, observer);
		    observer.onSubscribe(listener);
		    view.setOnClickListener(listener);
		}
		
		static final class Listener extends MainThreadDisposable implements OnClickListener {
		    private final View view;
		    private final Observer<? super Object> observer;
		
		Listener(View view, Observer<? super Object> observer) {
		  this.view = view;
		  this.observer = observer;
		}
		
		@Override public void onClick(View v) {
		  if (!isDisposed()) {
		    observer.onNext(Notification.INSTANCE);
		  }
		}
		
		@Override protected void onDispose() {
		  view.setOnClickListener(null);
		}
  		}
		}
		

		使用方法：
		1，对用View的各种基础事件如点击事件等，都是封装在RxView里的。
		2，对于某个View特有的事件封装在对应名称的类中，如RxTextView（TextWatcher）, RxSeekBar, RxToolBar等等
		3，对于各种单个参数的或者无参数的事件（如OnClickListener）， 都封装为XXXXXOnSubscribe , 比如:ViewClickOnSubscribe
		4，对于多个参数的事件都先把参数封装为xxxxEvent, 如：TextViewAfterTextChangeEvent。
		然后再封装一个XXXXOnSunscribe<xxxxEvent>, 这就等同于ViewClickOnSubscribe了
		
		
4， RxPermissions - 由RxJava支持的Android运行时权限（minSdkVersion must be >= 11）

	dependencies {
	    compile 'com.tbruyelle.rxpermissions2:rxpermissions:0.9.5@aar'
	}
	
	
	例子1：
	RxPermissions rxPermissions =  new  RxPermissions（this）; //这是一个Activity实例
	rxPermissions
	    .request(Manifest.permission.CAMERA)
	    .subscribe(granted -> {
	        if (granted) { // Always true pre-M
	           // I can control the camera now
	        } else {
	           // Oups permission denied
	        }
	    });


	例子2：
	RxView.clicks(findViewById(R.id.enableCamera))
    .compose(rxPermissions.ensure(Manifest.permission.CAMERA))
    .subscribe(granted -> {
        // R.id.enableCamera has been clicked
    });
    
    
	例子3：
	rxPermissions
    .requestEachCombined(Manifest.permission.CAMERA,
             Manifest.permission.READ_PHONE_STATE)
    .subscribe(permission -> { // will emit 1 Permission object
        if (permission.granted) {
           // All permissions are granted !
        } else if (permission.shouldShowRequestPermissionRationale)
           // At least one denied permission without ask never again
        } else {
           // At least one denied permission with ask never again
           // Need to go to the settings
        }
    });