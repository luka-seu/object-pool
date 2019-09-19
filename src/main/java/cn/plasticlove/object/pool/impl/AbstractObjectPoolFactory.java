package cn.plasticlove.object.pool.impl;

import cn.plasticlove.object.pool.ObjectPoolFactory;
import cn.plasticlove.object.pool.PooledObject;
import cn.plasticlove.object.pool.exception.ObjectDestroyException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 针对于{@link SimpleBaseObjectPool}对象池的对象工厂
 * 主要包括创建和销毁对象
 * 通过包装的方式创建对象的包装类
 * 如果需要工厂中更多的关于对象本身的操作，可以继承此类加以扩展
 *
 * @author luka-seu
 * @version 1.0
 **/

public abstract class AbstractObjectPoolFactory<T> implements ObjectPoolFactory<T> {
    /**
     * 创建对象池实际对象的抽象方法，不同子类可以重写不同逻辑
     * 也可以采用匿名内部类的方式在使用的时候重写该方法
     *
     * @return 实际的对象池对象
     * @throws Exception
     */
    public abstract T create() throws Exception;

    /**
     * 包装对象池对象成为{@link PooledObject}
     *
     * @param t 实际的对象池对象
     * @return
     */
    public abstract PooledObject<T> wrap(T t);

    /**
     * 在销毁对象时加的锁
     */
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public PooledObject<T> makeObject() throws Exception {
        return wrap(create());
    }

    @Override
    public void destroyObject(PooledObject<T> obj) throws ObjectDestroyException {
        //加锁是为了多个线程销毁同一个对象
        lock.lock();
        try {
            if (obj != null && obj.getObject() != null) {
                T object = obj.getObject();
                object = null;
            }
        } catch (Exception e) {
            throw new ObjectDestroyException();
        } finally {
            lock.unlock();
        }
    }

}
