package com.example.demo1.Thread2;

import com.example.demo1.Thread.Notify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Title: ThreadTest <br>
 * Description: ThreadTest <br>
 * Date: 2019年06月12日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public class ThreadTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ThreadTest.class);
    public static void main(String[] args) {
        test1();

    }

    private static void test1() {
        MyTreadPool myTreadPool = new MyTreadPool(3, 5, 3, TimeUnit.MILLISECONDS
                , new ArrayBlockingQueue<Runnable>(5), new MyRejectedExecutionHandler().new CallerRunsPolicy(), new Notify() {
            @Override
            public void notifyListen() {
                System.out.println("======任务全部执行完========");
            }
        });

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            myTreadPool.execute(()->{

                    LOGGER.info(Thread.currentThread().getName()+":"+finalI);

            });
        }
        myTreadPool.execute(()->{
                int x=1/0;
            LOGGER.info(Thread.currentThread().getName()+":");

        });
        myTreadPool.shutdown();
    }
    private static void test2() {
        MyTreadPool myTreadPool = new MyTreadPool(3, 5, 3, TimeUnit.MILLISECONDS
                , new ArrayBlockingQueue<Runnable>(5), new MyRejectedExecutionHandler().new CallerRunsPolicy(), new Notify() {
            @Override
            public void notifyListen() {
                System.out.println("======任务全部执行完========");
            }
        });
        List<MyFeatureTask<String>> list=new ArrayList<>();
        for (int i = 0; i <30; i++) {
            int finalI = i;
            MyFeatureTask<String> submit = myTreadPool.submit(new MCallable<String>() {
                @Override
                public String call() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "这是" + finalI;
                }
            });
            list.add(submit);

        }
        myTreadPool.shutdown();
        for (MyFeatureTask<String> stringMyFeatureTask : list) {
            try {
                System.out.println(stringMyFeatureTask.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
