package cn.plasticlove.object.pool;

/**
 * 对象池中对象所处于的状态
 * 包括空闲状态和使用中状态
 * @author luka-seu
 * @version 1.0
 **/

public enum PooledObjectState {
    /**
     * 空闲状态
     */
    IDEL(0,"idle"),
    /**
     * 使用状态
     */
    USING(1,"using");
    /**
     * code
     */
    private final int code;
    /**
     * msg
     */
    private final String msg;

    PooledObjectState(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
