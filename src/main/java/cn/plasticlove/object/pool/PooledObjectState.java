package cn.plasticlove.object.pool;

/**
 * @author luka-seu
 * @description
 * @create 2019-09 16-18:40
 **/

public enum PooledObjectState {
    IDEL(0,"idel"),
    USING(1,"using");
    private int code;
    private String msg;

    PooledObjectState(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
