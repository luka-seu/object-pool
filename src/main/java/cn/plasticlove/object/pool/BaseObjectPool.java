package cn.plasticlove.object.pool;


import cn.plasticlove.object.pool.exception.IllegalObjectStateException;
import cn.plasticlove.object.pool.exception.NoMoreIdleSpaceException;
import cn.plasticlove.object.pool.exception.ObjectDestroyException;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPool;

/**
 * 这是对象池的基本接口。提供了实现对象池的一些基本功能。
 * 对象池里的实例主要有对象池工厂 {@link ObjectPoolFactory}创建.
 * 如果需要实现自己的自定义对象池，可以实现这个接口加以扩展
 *
 * @author luka-seu
 * @version 1.0
 * @see SimpleBaseObjectPool 对象池的简单实现
 */
public interface BaseObjectPool<T> {
    /**
     * 向对象池中添加实例
     * 对于子类，实现这个方法需要注意添加的新实例都是添加在空闲队列中。
     * 必须保证空闲队列的长度不会超过对象池的最大容量
     */
    public void addObject();

    /**
     * 从对象池中获取实例
     * <p>
     * 对于子类，实现这个方法需要注意获取实例是从空闲队列中获取。
     * 同时获取之后，必须满足空闲队列里的空闲实例数不能小于最小值{@link SimpleBaseObjectPool#getMinIdel()}
     *
     * @return 对象实例
     * @throws NoMoreIdleSpaceException 当空闲队列为空时抛出异常
     */
    public T getObject() throws NoMoreIdleSpaceException;

    /**
     * 将实例返回到对象池中
     * 对象池中的对象实例可以被多次使用，因此，
     * 获取的对象使用完毕后需调用此方法还给对象池
     *
     * @param obj 待返还的对象
     * @throws IllegalObjectStateException 当待返还的对象不是正在使用的对象，抛出此异常.
     * @throws ObjectDestroyException      当待返还的实例因为某种原因（比如被其他线程从对象池中移除）不在对象池中，直接删除
     */
    public void returnObject(T obj) throws ObjectDestroyException, IllegalObjectStateException;

    /**
     * 获取对象池所能承载的最大对象实例数
     *
     * @return 对象池所能承载的最大对象实例数
     */
    public int getMaxNum();

    /**
     * 获取对象池活跃的对象实例数目
     * max num - idle num
     *
     * @return the num of active objects
     */
    public int getActiveNum();

    /**
     * 关闭对象池
     * 当对象池使用完毕后，不要忘记关闭对象池.
     */
    public void close();

    /**
     * 清除对象池中的空闲对象实例
     * 已经在使用的不会被清除
     *
     * @throws ObjectDestroyException
     */
    public void clear() throws ObjectDestroyException;

    /**
     * 判断当前对象池是否已经关闭
     *
     * @return
     */
    public boolean isClosed();

    /**
     * 判断对象池是否开启阻塞机制
     * 开启的话，如果没有空闲对象，会阻塞
     * 没有开启的情况下会直接返回异常
     *
     * @return
     */
    public boolean isBlocked();


    /**
     * 摧毁一个实例，让其被GC回收
     *
     * @param p 待摧毁的对象
     * @throws ObjectDestroyException
     */
    void destroy(PooledObject<T> p) throws ObjectDestroyException;

    /**
     * 重要的方法
     * 空闲对象必须有完整的生命周期，因此当空闲时间达到设定值时，需要清除空闲对象
     * 对于子类，可以另开一个和线程，实现定时任务定时检查正在运行的对象池中的空闲对象是否需要清除
     *
     * @throws ObjectDestroyException
     */
    public void removeTimeOutedObject() throws ObjectDestroyException;
}
