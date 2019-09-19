package cn.plasticlove.object.pool;


/**
 * 获取对象的方式，如LRU机制或LIFO机制
 * 如果有其他机制，可以继承此类进行扩展
 *
 * @author luka-seu
 * @version 1.0
 */
public class ObtainPolicy {

    public static final String LRU = "lru";
    public static final String LIFO = "lifo";
    /**
     * 获取机制
     */
    private String policy = LIFO;

    public ObtainPolicy(String policy) {
        this.policy = policy;
    }

    public ObtainPolicy() {
    }

    /**
     * 得到获取机制
     *
     * @return 获取机制
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * 设置获取机制
     *
     * @param policy 获取机制
     */
    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
