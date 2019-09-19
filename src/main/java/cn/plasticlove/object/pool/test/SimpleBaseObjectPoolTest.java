package cn.plasticlove.object.pool.test;

import cn.plasticlove.object.pool.PooledObject;
import cn.plasticlove.object.pool.exception.IllegalObjectStateException;
import cn.plasticlove.object.pool.exception.NoMoreIdleSpaceException;
import cn.plasticlove.object.pool.exception.ObjectDestroyException;
import cn.plasticlove.object.pool.impl.AbstractObjectPoolFactory;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPool;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPoolConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试类
 * @author luka-seu
 **/

public class SimpleBaseObjectPoolTest {
    private SimpleBaseObjectPoolConfig config;
    AbstractObjectPoolFactory factory;
    SimpleBaseObjectPool<Person> pool;


    @Before
    public void initTest() {
        config = new SimpleBaseObjectPoolConfig();
        config.setMinIdel(1);
        config.setMaxTotal(3);
        factory = new AbstractObjectPoolFactory() {
            @Override
            public Object create() throws Exception {
                return new Person();
            }

            @Override
            public PooledObject wrap(Object o) {
                return new PooledObject(o);
            }
        };
        pool = new SimpleBaseObjectPool<>(factory, config);
    }


    @Test
    public void testAddObject() {
        pool.addObject();
        pool.addObject();
        //当第四个加入的时候或报出异常，队列已满
        pool.addObject();
        /**
         * java.lang.IllegalStateException: Deque full
         *
         * 	at java.util.concurrent.LinkedBlockingDeque.addLast(LinkedBlockingDeque.java:335)
         * 	at cn.plasticlove.object.pool.impl.SimpleBaseObjectPool.addIdelQueue(SimpleBaseObjectPool.java:258)
         * 	at cn.plasticlove.object.pool.impl.SimpleBaseObjectPool.addObject(SimpleBaseObjectPool.java:215)
         * 	at cn.plasticlove.object.pool.test.SimpleBaseObjectPoolTest.testAddObject(SimpleBaseObjectPoolTest.java:44)
         * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
         * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
         * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
         * 	at java.lang.reflect.Method.invoke(Method.java:498)
         * 	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
         * 	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
         * 	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
         * 	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
         * 	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
         * 	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:305)
         * 	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
         * 	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:365)
         * 	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
         * 	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
         * 	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:330)
         * 	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:78)
         * 	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:328)
         * 	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:65)
         * 	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:292)
         * 	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:305)
         * 	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
         * 	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
         * 	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
         * 	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
         * 	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
         * 	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
         */
    }

    @Test
    public void testGetObject() throws InterruptedException {
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
    @Test
    public void testRemoveTimeoutObject() throws InterruptedException {
        Thread.sleep(1000*1000);
    }

}
