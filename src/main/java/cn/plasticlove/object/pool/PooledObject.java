package cn.plasticlove.object.pool;

/**
 * @author luka-seu
 * @description
 * @create 2019-09 16-18:06
 **/

public class PooledObject<T> {
    private final T obj;

    private PooledObjectState state;
    private long lastUsedTime;

    public PooledObjectState getState() {
        return state;
    }

    public void setState(PooledObjectState state) {
        this.state = state;
    }

    public PooledObject(T object) {
        this.obj = object;
    }

    public T getObject(){
        return this.obj;
    }
    public boolean isIdel(){
        return this.state==PooledObjectState.IDEL;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }
}
