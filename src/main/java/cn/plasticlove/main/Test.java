package cn.plasticlove.main;

import cn.plasticlove.object.pool.PooledObject;
import cn.plasticlove.object.pool.impl.SimpleBaseObjectPool;
import cn.plasticlove.object.pool.impl.SimpleObjectPoolFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author luka-seu
 * @description
 * @create 2019-09 16-18:44
 **/

public class Test {
    public static void main(String[] args) throws Exception {
//       PooledObjectFactory factory = new BasePooledObjectFactory() {
//           @Override
//           public Object create() throws Exception {
//               return new Person();
//           }
//
//           @Override
//           public PooledObject wrap(Object obj) {
//               return new DefaultPooledObject(obj);
//           }
//       };
//        GenericObjectPool pool =new GenericObjectPool(factory);
//        pool.close();
        SimpleObjectPoolFactory factory = new SimpleObjectPoolFactory() {
            @Override
            public Object create() throws Exception {
                return new Person();
            }

            @Override
            public PooledObject wrap(Object o) {
                return new PooledObject(o);
            }


        };
        SimpleBaseObjectPool<Person> pool = new SimpleBaseObjectPool<>(factory);
        Person p1 = pool.getObject();
        Person p2 = pool.getObject();
    }
}
