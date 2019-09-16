package cn.plasticlove.object.pool;

public class ObtainPolicy {

    public static final String LRU = "lru";
    public static final String LIFO = "lifo";

    private String policy = LIFO;

    public ObtainPolicy(String policy) {
        this.policy = policy;
    }

    public ObtainPolicy() {
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
