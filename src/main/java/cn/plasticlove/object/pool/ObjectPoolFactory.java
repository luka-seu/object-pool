package cn.plasticlove.object.pool;

import cn.plasticlove.object.pool.exception.ObjectDestroyException;
import cn.plasticlove.object.pool.impl.AbstractObjectPoolFactory;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPool;

/**
 * 用于创建和销毁对象池中对象实例的对象工厂接口.
 * 主要提供两个方法：创建和销毁;
 * 实现类可以根据不同的创建要求和销毁机制采取不同的实现.
 *
 * @author luka-seu
 * @version 1.0
 * @see  AbstractObjectPoolFactory 对象工厂的一种实现
 */
public interface ObjectPoolFactory<T> {

    /**
     * 创建对象，采用包装类的方式获取创建的对象
     *
     * @return 创建的包装类对象
     * @throws Exception 创建对象出现异常时，向上抛出
     * @see PooledObject
     */
    public PooledObject<T> makeObject() throws Exception;

    /**
     * 销毁对象
     *
     * @param obj 待销毁的对象包装类
     * @throws ObjectDestroyException 销毁对象时出现异常，向上抛出
     */
    public void destroyObject(PooledObject<T> obj) throws ObjectDestroyException;

}
