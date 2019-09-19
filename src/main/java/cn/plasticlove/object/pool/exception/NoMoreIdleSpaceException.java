package cn.plasticlove.object.pool.exception;

/**
 * 当获取对象时，没有阻塞机制的前提下
 * 对象池中无空闲对象可用时。抛出此异常
 *
 * @author luka-seu
 * @version 1.0
 **/

public class NoMoreIdleSpaceException extends Exception {
    public NoMoreIdleSpaceException() {
    }

    public NoMoreIdleSpaceException(String message) {
        super(message);
    }
}
