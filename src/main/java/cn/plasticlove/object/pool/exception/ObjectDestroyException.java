package cn.plasticlove.object.pool.exception;

/**
 * 在对象销毁时如果发生异常，抛出此异常
 *
 * @author luka-seu
 * @version 1.0
 **/

public class ObjectDestroyException extends Exception {
    public ObjectDestroyException() {
        super();
    }

    public ObjectDestroyException(String message) {
        super(message);
    }
}
