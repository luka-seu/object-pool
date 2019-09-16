package cn.plasticlove.object.pool.impl;

import cn.plasticlove.object.pool.BaseObjectPoolConfig;
import cn.plasticlove.object.pool.ObtainPolicy;

/**
 * @author luka-seu
 * @description
 * @create 2019-09 16-16:35
 **/

public class SimpleBaseObjectPoolConfig extends BaseObjectPoolConfig {
    public  static final int MAX_TOTAL = 5;
    public static final int MAX_IDEL = 3;
    public static final int MIN_IDEL = 2;

    private int maxTotal = MAX_TOTAL;
    private int maxIdel = MAX_IDEL;
    private int minIdel = MIN_IDEL;



    public SimpleBaseObjectPoolConfig() {
        super();
    }

    public SimpleBaseObjectPoolConfig(ObtainPolicy obtainPolicy, long maxIdleTime, long maxWaitTime, int maxTotal, int maxIdel, int minIdel) {
        super(obtainPolicy, maxIdleTime, maxWaitTime);
        this.maxTotal = maxTotal;
        this.maxIdel = maxIdel;
        this.minIdel = minIdel;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdel() {
        return maxIdel;
    }

    public void setMaxIdel(int maxIdel) {
        this.maxIdel = maxIdel;
    }

    public int getMinIdel() {
        return minIdel;
    }

    public void setMinIdel(int minIdel) {
        this.minIdel = minIdel;
    }
}
