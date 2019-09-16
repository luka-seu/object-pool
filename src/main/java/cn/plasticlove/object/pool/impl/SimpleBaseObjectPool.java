package cn.plasticlove.object.pool.impl;

import cn.plasticlove.object.pool.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * a simple implemention of ObjectPool
 *
 * @author luka-seu
 * @version 1.0
 **/

public class SimpleBaseObjectPool<T> implements BaseObjectPool<T> {
    private final ObjectPoolFactory factory;
    private LinkedBlockingDeque<PooledObject<T>> idelObjects;
    private volatile boolean closed = false;
    private volatile boolean blocked = false;

    private Map<PooledObjectWrap<T>,PooledObject<T>> allObjects = new ConcurrentHashMap<>();

    private volatile int maxTotal = SimpleBaseObjectPoolConfig.MAX_TOTAL;
    private volatile int maxIdel = SimpleBaseObjectPoolConfig.MAX_IDEL;
    private volatile int minIdel = SimpleBaseObjectPoolConfig.MIN_IDEL;
    private volatile ObtainPolicy obtainPolicy;
    private volatile long maxIdleTime;
    private volatile long maxWaitTime;
    private volatile long maxLiveTime;

    public SimpleBaseObjectPool(ObjectPoolFactory factory, SimpleBaseObjectPoolConfig config) {
        if (factory == null) {
            throw new NullPointerException("factory is null");
        }
        this.factory = factory;
        this.idelObjects = new LinkedBlockingDeque<>();
        if (config == null) {
            throw new IllegalArgumentException("the config for this kind of pool must be SimpleBaseObjectPoolConfig");
        }
        this.setConfig(config);
    }

    private void setConfig(SimpleBaseObjectPoolConfig config) {
        setMaxTotal(config.getMaxTotal());
        setMaxIdel(config.getMaxIdel());
        setMaxIdleTime(config.getMaxIdleTime());
        setMaxWaitTime(config.getMaxWaitTime());
        setMinIdel(config.getMinIdel());
        setObtainPolicy(config.getObtainPolicy());
    }

    public SimpleBaseObjectPool(ObjectPoolFactory factory) throws Exception {

        this(factory, new SimpleBaseObjectPoolConfig());
        initPool();
    }
    public void initPool(){
        if (getMinIdel()<1){
            return;
        }
        this.ensureMinIdel(getMinIdel());
    }

    private void ensureMinIdel(int idleCount) {
        if (idleCount < 1 || isClosed()) {
            return;
        }

        while (idelObjects.size() < idleCount) {
            if (allObjects.keySet().size()>=getMaxTotal()){
                blocked = true;
                break;
            }
            PooledObject<T> p = create();
            if (p == null) {
                break;
            }
            if (this.obtainPolicy.getPolicy().equals(ObtainPolicy.LIFO)) {
                idelObjects.addFirst(p);
            } else {
                idelObjects.addLast(p);
            }
            p.setState(PooledObjectState.IDEL);
            allObjects.put(new PooledObjectWrap<>(p.getObject()), p);
        }
        if (isClosed()) {

            clear();
        }
    }

    @Override
    public void addObject() {
        this.removeTimeOutedObject();
        this.assertOpen();
        if (factory == null) {
            throw new NullPointerException("factory is null");
        }
        PooledObject<T> obj = this.create();

        addIdelQueue(obj);
        obj.setLastUsedTime(System.currentTimeMillis());
        allObjects.put(new PooledObjectWrap<>(obj.getObject()),obj);


    }

    private PooledObject<T> create() {
        PooledObject<T> obj = null;
        try {
            obj = factory.makeObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        obj.setState(PooledObjectState.IDEL);

        return obj;
    }

    private void addIdelQueue(PooledObject<T> obj) {
        if (obj == null) {
            try {
                obj = factory.makeObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.obtainPolicy.getPolicy().equals(ObtainPolicy.LIFO)) {
            idelObjects.addFirst(obj);
        } else {
            idelObjects.addLast(obj);
        }

    }

    @Override
    public T getObject() {
        return this.getObject(getMaxWaitTime());
    }

    private T getObject(long maxTimeWaitMills) {
        this.removeTimeOutedObject();
        PooledObject<T> obj = null;

        if (blocked) {
            obj = idelObjects.pollFirst();
            if (obj == null) {
                try {
                    obj = factory.makeObject();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (obj == null) {
                if (maxTimeWaitMills < 0) {
                    try {
                        obj = idelObjects.takeFirst();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        obj = idelObjects.pollFirst(maxTimeWaitMills, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (obj == null) {
                throw new IllegalStateException("time out");
            }

        } else {
            obj = idelObjects.pollFirst();
            if (obj == null) {
                try {
                    obj = factory.makeObject();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (obj == null) {
                throw new IllegalStateException("make object failure");
            }
        }
        obj.setState(PooledObjectState.USING);
        ensureMinIdel(getMinIdel());
        return obj.getObject();
    }


    @Override
    public void returnObject(T obj) {
        PooledObject<T> p = allObjects.get(new PooledObjectWrap<>(obj));
        if (p==null){
            throw new IllegalStateException("the object is not part of the pool now");
        }
        final PooledObjectState state = p.getState();
        if (state!=PooledObjectState.USING){
            throw new IllegalStateException("the object is in wrong state");
        }
        if (isClosed()||idelObjects.size()>maxIdel){
            this.destroy(p);
        }else{
            if (this.obtainPolicy.getPolicy().equals(ObtainPolicy.LIFO)) {
                idelObjects.addFirst(p);
            } else {
                idelObjects.addLast(p);
            }
            p.setState(PooledObjectState.IDEL);
            p.setLastUsedTime(System.currentTimeMillis());
        }
    }

    @Override
    public boolean validateObject(T obj) {
        return false;
    }

    @Override
    public int getMaxNum() {
        return getMaxTotal();
    }

    @Override
    public int getActiveNum() {
        return this.maxTotal-idelObjects.size();
    }

    @Override
    public void close() {
        if (isClosed()){
            return;
        }
        clear();
        closed = true;
    }

    @Override
    public void clear() {
        PooledObject<T> p = idelObjects.poll();

        while (p != null) {
            try {
                destroy(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
            p = idelObjects.poll();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public void destroy(PooledObject<T> p) {
        if (p!=null) {
            p = null;
        }

    }

    @Override
    public void removeTimeOutedObject() {
        long now = System.currentTimeMillis();
        long timeout = now-getMaxLiveTime();
        ArrayList<PooledObject<T>> removeList = new ArrayList<>();
        Iterator<PooledObject<T>> iterator = allObjects.values().iterator();
        while(iterator.hasNext()){
            PooledObject<T> next = iterator.next();
            if (next.getState()==PooledObjectState.IDEL&&next.getLastUsedTime()<=timeout){
                removeList.add(next);
            }
        }
        Iterator<PooledObject<T>> removeIt = removeList.iterator();
        while(removeIt.hasNext()){
            PooledObject<T> nextRemove = removeIt.next();
            if (nextRemove.getState()==PooledObjectState.IDEL){
                destroy(nextRemove);
            }
        }
        ensureMinIdel(getMinIdel());

    }

    private boolean assertOpen() {
        if (isClosed()) {
            throw new IllegalStateException("pool is not open");
        }
        return true;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdel() {
        return maxIdel;
    }

    public void setMaxIdel(int maxIdel) {
        this.maxIdel = maxIdel;
    }

    public int getMinIdel() {
        return minIdel;
    }

    public long getMaxLiveTime() {
        return maxLiveTime;
    }

    public void setMaxLiveTime(long maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }

    public void setMinIdel(int minIdel) {
        this.minIdel = minIdel;
    }

    public ObtainPolicy getObtainPolicy() {
        return obtainPolicy;
    }

    public void setObtainPolicy(ObtainPolicy obtainPolicy) {
        this.obtainPolicy = obtainPolicy;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }


    static class PooledObjectWrap<T> {
        private final T obj;

        PooledObjectWrap(T obj) {
            this.obj = obj;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PooledObjectWrap<?> that = (PooledObjectWrap<?>) o;
            return Objects.equals(obj, that.obj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(obj);
        }
    }
}
