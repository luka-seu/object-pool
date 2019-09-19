package cn.plasticlove.object.pool.samplecode;

import cn.plasticlove.object.pool.PooledObject;
import cn.plasticlove.object.pool.exception.IllegalObjectStateException;
import cn.plasticlove.object.pool.exception.NoMoreIdleSpaceException;
import cn.plasticlove.object.pool.exception.ObjectDestroyException;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPool;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPoolConfig;
import cn.plasticlove.object.pool.impl.AbstractObjectPoolFactory;
import cn.plasticlove.object.pool.test.Person;


/**
 * sample code
 * @author luka-seu
 **/

public class SampleCode {
    public static void main(String[] args) throws Exception {

        SimpleBaseObjectPoolConfig config = new SimpleBaseObjectPoolConfig();
        config.setMinIdel(1);
        config.setMaxTotal(3);
        AbstractObjectPoolFactory factory = new AbstractObjectPoolFactory() {
            @Override
            public Object create() throws Exception {
                return new Person();
            }

            @Override
            public PooledObject wrap(Object o) {
                return new PooledObject(o);
            }
        };
        SimpleBaseObjectPool<Person> pool = new SimpleBaseObjectPool<>(factory, config);

        //开五个线程获取对象
        //1. 首先t2线程获取对象，
        //2. 隔1s后t1线程获取对象
        //3. 再隔1s后t3线程获取对象
        //4. 再隔1s后t4线程获取对象
        //5. 再隔1s后t5线程获取对象
        //6. 1步骤后隔8秒t2线程返还对象

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Person p1 = null;
                try {
                    p1 = pool.getObject();
                } catch (NoMoreIdleSpaceException e) {
                    e.printStackTrace();
                }
                System.out.println(pool.getActiveNum());
                if (p1!=null) {
                    System.out.println(p1.hashCode());
                }
            }
        }, "t1");
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                Person p2 = null;
                try {
                    p2 = pool.getObject();
                } catch (NoMoreIdleSpaceException e) {
                    e.printStackTrace();
                }
                System.out.println(pool.getActiveNum());
                if (p2!=null) {
                    System.out.println(p2.hashCode());
                }
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    pool.returnObject(p2);
                } catch (ObjectDestroyException e) {
                    e.printStackTrace();
                } catch (IllegalObjectStateException e) {
                    e.printStackTrace();
                }
                System.out.println(pool.getActiveNum());
            }
        }, "t2");
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Person p3 = null;
                try {
                    p3 = pool.getObject();
                } catch (NoMoreIdleSpaceException e) {
                    e.printStackTrace();
                }
                System.out.println(pool.getActiveNum());
                if (p3!=null) {
                    System.out.println(p3.hashCode());
                }
            }
        }, "t3");
        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Person p4 = null;
                try {
                    p4 = pool.getObject();
                } catch (NoMoreIdleSpaceException e) {
                    e.printStackTrace();
                }
                System.out.println(pool.getActiveNum());
                if (p4!=null) {
                    System.out.println(p4.hashCode());
                }
            }
        }, "t4");
        Thread t5 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Person p5 = null;
                try {
                    p5 = pool.getObject();
                } catch (NoMoreIdleSpaceException e) {
                    e.printStackTrace();
                }
                System.out.println(pool.getActiveNum());
                if (p5!=null) {
                    System.out.println(p5.hashCode());
                }
            }
        }, "t5");
        t2.start();
        t1.start();
        t3.start();
        t4.start();
        t5.start();
        Thread.sleep(60000);

    }


}
