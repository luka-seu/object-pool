package cn.plasticlove.object.pool.impl;

import cn.plasticlove.object.pool.BaseObjectPoolConfig;
import cn.plasticlove.object.pool.ObtainPolicy;

/**
 * 针对{@link SimpleBaseObjectPool}的配置类
 * 相比于{@link BaseObjectPoolConfig} 增加了对象池最大容量
 * 最小空闲对象数，以及是否采用阻塞机制
 *
 * @author luka-seu
 * @version 1.0
 **/

public class SimpleBaseObjectPoolConfig extends BaseObjectPoolConfig {
    /**
     * 对象池最大容量默认值
     */
    static final int MAX_TOTAL = 5;
    /**
     * 对象池最小空闲对象数默认值
     */
    static final int MIN_IDEL = 2;
    /**
     * 对象池最大容量
     */
    private int maxTotal = MAX_TOTAL;
    /**
     * 对象池最小空闲对象数
     */
    private int minIdel = MIN_IDEL;
    /**
     * 是否使用阻塞机制
     * 默认为true
     */
    private boolean blocked = true;


    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public SimpleBaseObjectPoolConfig() {
        super();
    }

    public SimpleBaseObjectPoolConfig(ObtainPolicy obtainPolicy, long maxWaitTime, long maxLiveTime, int maxTotal, int minIdel, boolean blocked) {
        super(obtainPolicy, maxWaitTime, maxLiveTime);
        this.maxTotal = maxTotal;

        this.minIdel = minIdel;
        this.blocked = blocked;
    }

    /**
     * 获取当前对象池最大容量
     *
     * @return 当前对象池最大容量
     */
    public int getMaxTotal() {
        return maxTotal;
    }
    /**
     * 设置当前对象池最大容量
     *
     * @param maxTotal 当前对象池最大容量
     */
    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    /**
     * 设置最小空闲对象数
     * @param minIdel 最小空闲对象数
     */
    public void setMinIdel(int minIdel) {
        this.minIdel = minIdel;
    }

    /**
     * 获取当前对象池的最小空闲对象数
     *
     * @return 当前对象池的最小空闲对象数
     */
    public int getMinIdel() {
        return minIdel;
    }


}
