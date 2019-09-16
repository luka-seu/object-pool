package cn.plasticlove.object.pool;

public interface ObjectPoolFactory<T> {


    public PooledObject<T> makeObject() throws Exception;

    public void destroyObject(PooledObject<T> obj);

    public boolean validateObject(PooledObject<T> obj);

    public void activeObject(PooledObject<T> obj);

    public void passiveObject(PooledObject<T> obj);
}
