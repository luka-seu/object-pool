package cn.plasticlove.object.pool.exception;

/**
 * 对象状态不对，在返还一个对象时对象应处于使用中状态
 * 同时在判断一个对象是否是过期对象时应处于空闲状态
 *
 * @author luka-seu
 * @version 1.0
 * @see cn.plasticlove.object.pool.PooledObjectState
 **/

public class IllegalObjectStateException extends Exception {
    public IllegalObjectStateException() {
    }

    public IllegalObjectStateException(String message) {
        super(message);
    }
}
