package cn.plasticlove.object.pool;

/**
 * 对象池对象的包装类，通过包装类给对象赋予其他的属性
 * 比如当前状态{@link PooledObjectState}
 * 以及上次使用时间，用来判断空闲对象是否达到最大空闲时间
 *
 * @author luka-seu
 * @version 1.0
 **/

public class PooledObject<T> {
    /**
     * 真实的对象池对象
     */
    private final T obj;
    /**
     * 当前状态{@link PooledObjectState}
     */
    private PooledObjectState state;
    /**
     * 上次使用时间
     */
    private long lastUsedTime;

    /**
     * 获取当前对象状态
     *
     * @return 当前对象状态
     */
    public PooledObjectState getState() {
        return state;
    }

    /**
     * 设置当前对象状态
     *
     * @param state 当前对象状态
     */
    public void setState(PooledObjectState state) {
        this.state = state;
    }

    public PooledObject(T object) {
        this.obj = object;
    }

    /**
     * 获取对象池实际对象
     *
     * @return 对象池实际对象
     */
    public T getObject() {
        return this.obj;
    }

    /**
     * 判断当前对象是否是空闲状态
     *
     * @return 当前对象是否是空闲状态
     */
    public boolean isIdel() {
        return this.state == PooledObjectState.IDEL;
    }

    /**
     * 获取对象的上次使用时间（开始空闲的时间）
     *
     * @return 对象的上次使用时间（开始空闲的时间）
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    /**
     * 设置对象的上次使用时间（开始空闲的时间）
     *
     * @param lastUsedTime 对象的上次使用时间（开始空闲的时间）
     */
    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }
}
