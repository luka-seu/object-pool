package cn.plasticlove.object.pool.impl;

import cn.plasticlove.object.pool.*;
import cn.plasticlove.object.pool.exception.IllegalObjectStateException;
import cn.plasticlove.object.pool.exception.NoMoreIdleSpaceException;
import cn.plasticlove.object.pool.exception.ObjectDestroyException;
import cn.plasticlove.object.pool.util.LogUtil;

import java.util.*;
import java.util.concurrent.*;

/**
 * 对象池{@link BaseObjectPool}的一个实现
 * 整个对象池的工作流程可以归结为：
 * <p>
 * 1.新建对象池
 * </p>
 * <p>
 * 2.初始化对象池
 * </p>
 *
 * <p>(1). 包括将{@link SimpleBaseObjectPoolConfig}的配置设置进来。</p>
 * <p>(2). 初始化空闲对象队列，确保其不低于最小空闲对象数</p>
 * <p>(3). 开启监控空闲对象生命周期的定时任务</p>
 * <p>3.使用对象池</p>
 * <p>(1). 对象池初始化出来会有{@link SimpleBaseObjectPool#minIdel}个空闲对象，存放在{@link SimpleBaseObjectPool#idleObjects}队列中；整个对象池也会有{@link SimpleBaseObjectPool#minIdel}个对象，存放在{@link SimpleBaseObjectPool#allObjects}的map中</p>
 * <p>(2). 每次获取对象，会从空闲队列中获取，这是如果空闲队列长度小于{@link SimpleBaseObjectPool#minIdel}，会新建对象加入空闲队列中，也会加入到整个对象池的{@link SimpleBaseObjectPool#allObjects}中</p>
 * <p>(3). 当{@link SimpleBaseObjectPool#allObjects}长度达到{@link SimpleBaseObjectPool#maxTotal}时，就不允许新建对象。{@link SimpleBaseObjectPool#idleObjects}的长度会随着线程获取对象而减少，当{@link SimpleBaseObjectPool#idleObjects}长度为零时，获取对象的线程会被阻塞</p>
 * <p>(4). 空闲对象超过{@link SimpleBaseObjectPool#maxLiveTime}会被回收.但仍需要保证对象池的最小空闲对象数。</p>
 *
 * @author luka-seu
 * @version 1.0
 **/

public class SimpleBaseObjectPool<T> implements BaseObjectPool<T> {
    private static final String TAG = SimpleBaseObjectPool.class.getSimpleName();
    /**
     * 对象工厂，用于对象池中对象的创建和销毁
     */
    private final AbstractObjectPoolFactory factory;
    /**
     * 空闲对象队列
     */
    private LinkedBlockingDeque<PooledObject<T>> idleObjects;
    /**
     * 当前线程池是否处于关闭状态
     */
    private volatile boolean closed = false;
    /**
     * 是否开启阻塞机制
     */
    private volatile boolean blocked = false;
    /**
     * 存放所有对象的map
     * 使用map是为了在返还对象的时候可以检查对象是否还存在于对象池中
     */
    private Map<PooledObjectWrap<T>, PooledObject<T>> allObjects = new ConcurrentHashMap<>();

    /**
     * 对象池中所能容纳的最大对象数
     */
    private volatile int maxTotal = SimpleBaseObjectPoolConfig.MAX_TOTAL;
    /**
     * 对象池中最小空闲线程数
     */
    private volatile int minIdel = SimpleBaseObjectPoolConfig.MIN_IDEL;
    /**
     * 从对象池中获取对象的策略
     */
    private volatile ObtainPolicy obtainPolicy;
    /**
     * 阻塞等待的最长时间
     */
    private volatile long maxWaitTime;
    /**
     * 空闲对象的最长生命周期
     */
    private volatile long maxLiveTime;
    /**
     * 清除过期空闲对象的定时任务
     */
    private ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();

    /**
     * @param factory 对象工厂
     * @param config  自定义的对象池配置
     */
    public SimpleBaseObjectPool(AbstractObjectPoolFactory factory, SimpleBaseObjectPoolConfig config) {

        LogUtil.info(TAG, String.format("the pool started at %s", new Date()));

        if (factory == null) {
            LogUtil.info(TAG, "SimpleObjectPoolFactory must not be null");
            throw new IllegalStateException("factory is null");

        }
        this.factory = factory;

        if (config == null) {
            LogUtil.info(TAG, "SimpleBaseObjectPoolConfig must not be null");
            throw new IllegalArgumentException("the config for this kind of pool must be SimpleBaseObjectPoolConfig");
        }
        this.setConfig(config);
        this.idleObjects = new LinkedBlockingDeque<>(getMaxTotal());
        initPool();
        removeTask();

    }

    /**
     * 根据配置类的参数初始化对象池
     *
     * @param config 配置类
     */
    private void setConfig(SimpleBaseObjectPoolConfig config) {
        setMaxTotal(config.getMaxTotal());
        setMaxWaitTime(config.getMaxWaitTime());
        setMinIdel(config.getMinIdel());
        setObtainPolicy(config.getObtainPolicy());
        setBlocked(config.isBlocked());
        setMaxLiveTime(config.getMaxLiveTime());
    }

    /**
     * @param factory 对象工厂
     * @throws Exception
     */
    public SimpleBaseObjectPool(AbstractObjectPoolFactory factory) throws Exception {

        this(factory, new SimpleBaseObjectPoolConfig());

    }

    /**
     * 初始化对象池的最小空闲对象数
     */
    private void initPool() {
        //当设置的最小空闲对象数小于1，直接返回
        if (getMinIdel() < 1) {
            return;
        }
        this.ensureMinIdel(getMinIdel());
        LogUtil.info(TAG, String.format("pool is inited. min idle objects is %d; max num of objects is %d; there are %d idle objects for use",
                getMinIdel(), getMaxTotal(), idleObjects.size()));
    }

    /**
     * 产生数量为idleCount的最小空闲对象数加入到空闲对象队列中
     *
     * @param idleCount 最小空闲对象数
     */
    private synchronized void ensureMinIdel(int idleCount) {

        if (idleCount < 1 || isClosed()) {
            return;
        }

        while (idleObjects.size() < idleCount) {
            LogUtil.info(TAG, String.format("the num of idle objects %d is less than minIdle %d, create new idle objects", idleObjects.size(), idleCount));
            //当对象池对象数超过最大值，不再确保最小空闲对象数
            if (allObjects.keySet().size() >= getMaxTotal()) {
                LogUtil.info(TAG, String.format("the pool size %d reached to max num %d! Create new idle object fail!", allObjects.keySet().size(), getMaxTotal()));
                LogUtil.info(TAG, String.format("idle num of objects is %d", idleObjects.size()));
                blocked = true;
                break;
            }
            PooledObject<T> p = create();
            if (p == null) {
                break;
            }
            if (this.obtainPolicy.getPolicy().equals(ObtainPolicy.LIFO)) {
                idleObjects.addFirst(p);
            } else {
                idleObjects.addLast(p);
            }
            //从设置为空闲状态开始的时间设为上次使用的时间点
            p.setLastUsedTime(System.currentTimeMillis());
            allObjects.put(new PooledObjectWrap<>(p.getObject()), p);
            LogUtil.info(TAG, String.format("now idle num and total num of objects is %d and %d", idleObjects.size(), allObjects.keySet().size()));
        }
        //防止其他线程已经关闭对象池，如果关闭，将对象池清空。防止内存泄露
        if (isClosed()) {
            clear();
        }
    }

    /**
     * 设置是否阻塞机制
     *
     * @param blocked
     */
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    /**
     * 向对象池添加对象
     */
    @Override
    public void addObject() {
        this.assertOpen();
        if (factory == null) {
            throw new IllegalStateException("factory is null");
        }
        PooledObject<T> obj = this.create();

        addIdelQueue(obj);
        obj.setLastUsedTime(System.currentTimeMillis());
        allObjects.put(new PooledObjectWrap<>(obj.getObject()), obj);


    }

    /**
     * 调用对象工厂创建对象
     *
     * @return 对象池对象的包装类
     */
    private PooledObject<T> create() {
        PooledObject<T> obj = null;
        try {
            obj = factory.makeObject();
        } catch (Exception e) {
            LogUtil.info(TAG, "error when create object" + e.getMessage());
            e.printStackTrace();
        }
        //新创建的对象默认都是空闲状态
        if (obj != null) {
            obj.setState(PooledObjectState.IDEL);
        }

        return obj;
    }

    /**
     * 将对象加入到空闲队列中
     *
     * @param obj 待加入的对象包装类
     */
    private void addIdelQueue(PooledObject<T> obj) {
        if (obj == null) {
            try {
                obj = factory.makeObject();
            } catch (Exception e) {
                LogUtil.info(TAG, "error when create object" + e.getMessage());
                e.printStackTrace();
            }
        }
        if (this.obtainPolicy.getPolicy().equals(ObtainPolicy.LIFO)) {
            idleObjects.addFirst(obj);
        } else {
            idleObjects.addLast(obj);
        }

    }

    /**
     * 获取对象
     *
     * @return 需要的对象
     * @throws NoMoreIdleSpaceException
     */
    @Override
    public T getObject() throws NoMoreIdleSpaceException {
        return this.getObject(getMaxWaitTime());
    }

    /**
     * 从对象池中获取对象
     *
     * @param maxTimeWaitMills 阻塞时的最长等待时间
     * @return 需要的对象
     * @throws NoMoreIdleSpaceException
     */
    private T getObject(long maxTimeWaitMills) throws NoMoreIdleSpaceException {
        LogUtil.info(TAG, "start getting object");
        PooledObject obj = null;
        //首先判断是否设置了阻塞机制
        if (blocked) {
            LogUtil.info(TAG, String.format("blocked queue,wait time is %s mills", getMaxWaitTime()));
            obj = idleObjects.pollFirst();
            if (obj == null) {
                //如果空闲队列没有，且还未达到最大容量就新建对象
                if (allObjects.size()<getMaxTotal()){
                    LogUtil.info(TAG, String.format("no idle objects, create new object, the num of objects in pool is %d", allObjects.size()));
                    try {
                        obj = factory.makeObject();
                        allObjects.put(new PooledObjectWrap<T>((T) obj.getObject()),obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //表明对象池已满，且无空闲对象
                }else {
                    //当阻塞机制设为负值，可以一直等待下去
                    if (maxTimeWaitMills < 0) {
                        try {
                            obj = idleObjects.takeFirst();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            obj = idleObjects.pollFirst(maxTimeWaitMills, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            LogUtil.info(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            //达到最长等待时间还未获取到对象
            if (obj == null) {
                LogUtil.info(TAG, "get object fail");
                throw new NoMoreIdleSpaceException("there is no more idle object and the wait time is out");
            }
            //没有设置阻塞机制
        } else {
            LogUtil.info(TAG, "not blocked queue");
            obj = idleObjects.pollFirst();
            if (obj == null) {
                LogUtil.info(TAG, "get object fail");
                throw new IllegalStateException("make object failure");
            }
        }
        obj.setState(PooledObjectState.USING);
        LogUtil.info(TAG, "get object successfully");
        //确保空闲对象数不少于最小值
        ensureMinIdel(getMinIdel());

        return (T) obj.getObject();
    }

    /**
     * 向对象池中返还对象
     *
     * @param obj 待返还的对象
     * @throws ObjectDestroyException
     * @throws IllegalObjectStateException
     */
    @Override
    public void returnObject(T obj) throws ObjectDestroyException, IllegalObjectStateException {
        LogUtil.info(TAG, "returning object");
        //判断对象是否还属于对象池
        PooledObject<T> p = allObjects.get(new PooledObjectWrap<>(obj));
        if (p == null) {
            throw new IllegalStateException("the object is not part of the pool now");
        }
        final PooledObjectState state = p.getState();
        //同步是防止其他线程改变对象状态
        synchronized (p) {
            if (state != PooledObjectState.USING) {
                LogUtil.info(TAG, "state of Object is not using, cannot return to pool");
                throw new IllegalObjectStateException("the object is in wrong state");
            }
        }
        //如果对象池关闭或者空闲对象数达到或超过超过对象池最大容量，直接销毁该对象
        if (isClosed() || idleObjects.size() >= maxTotal) {
            this.destroy(p);
        } else {
            if (this.obtainPolicy.getPolicy().equals(ObtainPolicy.LIFO)) {
                idleObjects.addFirst(p);
            } else {
                idleObjects.addLast(p);
            }
            p.setState(PooledObjectState.IDEL);
            p.setLastUsedTime(System.currentTimeMillis());
        }
        LogUtil.info(TAG, "finish return object");
    }

    /**
     * 获取对象池最大容量
     *
     * @return
     */
    @Override
    public int getMaxNum() {
        return getMaxTotal();
    }

    /**
     * 获取对象池活跃的（正在使用）的对象数
     *
     * @return
     */
    @Override
    public int getActiveNum() {
        return allObjects.size() - idleObjects.size();
    }

    /**
     * 关闭对象池的同步锁
     */
    private final Object closeLock = new Object();

    /**
     * 关闭对象池
     */
    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        //同步是为了防止多个线程同时关闭对象池
        synchronized (closeLock) {
            clear();
            //关闭清除过期空闲对象的定时任务
            service.shutdownNow();
            closed = true;
            LogUtil.info(TAG, String.format("pool is closed at %s", new Date()));
        }
    }

    /**
     * 清除对象池中所有的空闲对象
     */
    @Override
    public void clear() {
        PooledObject<T> p = idleObjects.poll();

        while (p != null) {
            try {
                destroy(p);
            } catch (ObjectDestroyException e) {
                e.printStackTrace();
            }

            p = idleObjects.poll();
        }
    }

    /**
     * 判断是否已经关闭对象池
     *
     * @return
     */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * 判断是否设置阻塞机制
     *
     * @return
     */
    @Override
    public boolean isBlocked() {
        return blocked;
    }

    /**
     * 销毁对象
     *
     * @param p 待摧毁的对象
     * @throws ObjectDestroyException
     */
    @Override
    public void destroy(PooledObject<T> p) throws ObjectDestroyException {
        LogUtil.info(TAG, "start to destroy object");
        idleObjects.remove(p);
        allObjects.remove(new PooledObjectWrap<>(p.getObject()));
        factory.destroyObject(p);

    }

    /**
     * 用于清除过期空闲对象的task
     */
    private void removeTask() {


        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtil.info(TAG, Thread.currentThread().getName() + " start to clear the idle objects which are time out");
                    removeTimeOutedObject();
                } catch (ObjectDestroyException e) {
                    e.printStackTrace();
                }
            }
        };
        //延后10s触发，之后每隔60s执行一次
        service.scheduleAtFixedRate(task, 10, 60, TimeUnit.SECONDS);
    }

    /**
     * 清除对象池的过期空闲对象
     *
     * @throws ObjectDestroyException
     */
    @Override
    public void removeTimeOutedObject() throws ObjectDestroyException {
        long now = System.currentTimeMillis();
        long timeout = now - getMaxLiveTime();
        ArrayList<PooledObject<T>> removeList = new ArrayList<>();
        Iterator<PooledObject<T>> iterator = allObjects.values().iterator();
        while (iterator.hasNext()) {
            PooledObject<T> next = iterator.next();
            if (next.getState() == PooledObjectState.IDEL && next.getLastUsedTime() <= timeout) {
                removeList.add(next);
            }
        }
        Iterator<PooledObject<T>> removeIt = removeList.iterator();
        while (removeIt.hasNext()) {
            PooledObject<T> nextRemove = removeIt.next();
            if (nextRemove.getState() == PooledObjectState.IDEL) {
                destroy(nextRemove);
            }
        }
        //清除后要确保满足最小空闲对象数
        ensureMinIdel(getMinIdel());

    }

    /**
     * 确保对象池时开着的状态
     *
     * @return
     */
    private boolean assertOpen() {
        if (isClosed()) {
            throw new IllegalStateException("pool is not open");
        }
        return true;
    }

    /**
     * 获取当前对象池最大容量
     *
     * @return 当前对象池最大容量
     */
    private int getMaxTotal() {
        return maxTotal;
    }

    /**
     * 设置当前对象池的最大容量
     *
     * @param maxTotal 当前对象池的最大容量
     */
    private void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    /**
     * 获取当前对象池的最小空闲对象数
     *
     * @return 当前对象池的最小空闲对象数
     */
    public int getMinIdel() {
        return minIdel;
    }

    /**
     * 获取当前对象池的空闲对象的最长生命周期
     *
     * @return 当前对象池的空闲对象的最长生命周期
     */
    public long getMaxLiveTime() {
        return maxLiveTime;
    }

    /**
     * 设置当前对象池的空闲对象的最长生命周期
     *
     * @param maxLiveTime 当前对象池的空闲对象的最长生命周期
     */
    private void setMaxLiveTime(long maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }

    /**
     * 设置当前对象池的最小空闲对象数
     *
     * @param minIdel 当前对象池的最小空闲对象数
     */
    private void setMinIdel(int minIdel) {
        this.minIdel = minIdel;
    }

    /**
     * 获取当前对象池获取对象策略
     *
     * @return 当前对象池获取对象策略
     */
    public ObtainPolicy getObtainPolicy() {
        return obtainPolicy;
    }

    /**
     * 设置当前对象池获取对象策略
     *
     * @param obtainPolicy 当前对象池获取对象策略
     */
    private void setObtainPolicy(ObtainPolicy obtainPolicy) {
        this.obtainPolicy = obtainPolicy;
    }

    /**
     * 获取当前对象池最长阻塞时间
     *
     * @return 当前对象池最长阻塞时间
     */
    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * 设置当前对象池最长阻塞时间
     *
     * @param maxWaitTime 当前对象池最长阻塞时间
     */
    private void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    /**
     * 用于存放所有对象的map的键
     * 使用的也是包装类
     *
     * @param <T>
     */
    static class PooledObjectWrap<T> {
        private final T instance;


        public PooledObjectWrap(T instance) {
            this.instance = instance;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(instance);
        }

        @Override
        public boolean equals(Object other) {
            return ((PooledObjectWrap) other).instance == instance;
        }


        public T getObject() {
            return instance;
        }
    }
}
