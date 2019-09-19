package cn.plasticlove.object.pool;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPoolConfig;

/**
 * 对象池的配置类。
 * 用于配置对象的相关参数。
 * 如果需要扩展其他对象池参数，可以继承这个类。
 *
 * @author luka-seu
 * @version 1.0
 * @see SimpleBaseObjectPoolConfig SimpleBaseObjectPoolConfig专属配置类
 **/

public class BaseObjectPoolConfig {

    /**
     * 对象的最大等待时间默认值
     */
    private static final long MAX_WAIT_TIME_MILLS = 1000 * 10L;
    /**
     * 空闲对象的最大生命周期默认值
     */
    private static final long MAX_LIVE_TIME_MILLS = 1000 * 60L;
    /**
     * 获取对象策略(例如LRU或LIFO)
     *
     * @see ObtainPolicy
     */
    private ObtainPolicy obtainPolicy = new ObtainPolicy("LIFO");
    /**
     * 获取对象的最大等待时间
     */
    private long maxWaitTime = MAX_WAIT_TIME_MILLS;
    /**
     * 空闲对象的最大生命周期
     * {@link BaseObjectPool#removeTimeOutedObject()}
     */
    private long maxLiveTime = MAX_LIVE_TIME_MILLS;

    /**
     * 构造方法
     *
     * @param obtainPolicy 获取策略
     * @param maxWaitTime  最大等待时间
     * @param maxLiveTime  最长生命周期
     */
    public BaseObjectPoolConfig(ObtainPolicy obtainPolicy, long maxWaitTime, long maxLiveTime) {
        this.obtainPolicy = obtainPolicy;
        this.maxWaitTime = maxWaitTime;
        this.maxLiveTime = maxLiveTime;
    }

    /**
     * 无参构造方法
     */
    public BaseObjectPoolConfig() {
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
    public void setMaxLiveTime(long maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
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
    public void setObtainPolicy(ObtainPolicy obtainPolicy) {
        this.obtainPolicy = obtainPolicy;
    }

    /**
     * 获取当前对象池的空闲对象的最长生命周期
     *
     * @return 当前对象池的空闲对象的最长生命周期
     */
    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    /**
     * 获取当前对象池最长阻塞时间
     *
     * @param maxWaitTime 当前对象池最长阻塞时间
     */
    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }
}
