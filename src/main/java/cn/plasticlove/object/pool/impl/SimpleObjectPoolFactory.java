package cn.plasticlove.object.pool.impl;

import cn.plasticlove.object.pool.ObjectPoolFactory;
import cn.plasticlove.object.pool.PooledObject;

/**
 * @author luka-seu
 * @description
 * @create 2019-09 16-16:55
 **/

public abstract class SimpleObjectPoolFactory<T> implements ObjectPoolFactory<T> {

    public abstract T create() throws Exception;

    public abstract PooledObject<T> wrap(T t);

    @Override
    public PooledObject<T> makeObject() throws Exception {
        return wrap(create());
    }

    @Override
    public void destroyObject(PooledObject<T> obj) {

    }

    @Override
    public boolean validateObject(PooledObject<T> obj) {
        return false;
    }

    @Override
    public void activeObject(PooledObject<T> obj) {

    }

    @Override
    public void passiveObject(PooledObject<T> obj) {

    }
}
