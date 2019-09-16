package cn.plasticlove.object.pool;



/**
 * the base interface for object pool; which provides some base methods.
 * If you want to customrize your own object pool, you can impl this interface.
 * @author luka-seu
 * @version 1.0
 */
public interface BaseObjectPool<T> {
    /**
     * add the object
     *
     */
    public void addObject();

    /**
     * get the object
     */
    public T getObject();

    /**
     * return the object to the pool
     * @param obj the object to return
     */
    public void  returnObject(T obj);

    /**
     * validate the object
     * @param obj the object to validate
     */
    public boolean validateObject(T obj);

    /**
     * get the max num of objects in the pool
     */
    public int getMaxNum();

    /**
     * get the active num of objects in the poo;
     */
    public int getActiveNum();


    public void close();

    public void clear();

    public boolean isClosed();
    public boolean isBlocked();

    void destroy(PooledObject<T> p);

    public  void removeTimeOutedObject();
}
