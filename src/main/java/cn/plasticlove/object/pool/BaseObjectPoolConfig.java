package cn.plasticlove.object.pool;

/**
 * the base config of my object pool.
 * if you want to customerize the pool config,
 * you can extend this base class
 *
 * @author luka-seu
 * @version 1.0
 **/

public class BaseObjectPoolConfig {

    private static final long MAX_IDLE_TIME_MILLS = 3000L;
    private static final long MAX_WAIT_TIME_MILLS = 200L;

    private ObtainPolicy obtainPolicy = new ObtainPolicy("LIFO");
    private long maxIdleTime = MAX_IDLE_TIME_MILLS;
    private long maxWaitTime = MAX_WAIT_TIME_MILLS;

    public BaseObjectPoolConfig(ObtainPolicy obtainPolicy, long maxIdleTime, long maxWaitTime) {

        this.obtainPolicy = obtainPolicy;
        this.maxIdleTime = maxIdleTime;
        this.maxWaitTime = maxWaitTime;
    }

    public BaseObjectPoolConfig() {
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
}
