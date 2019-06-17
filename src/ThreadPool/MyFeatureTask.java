package com.example.demo1.Thread2;

import com.sun.org.apache.xalan.internal.utils.FeatureManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Title: MyFeatureTask <br>
 * Description: MyFeatureTask <br>
 * Date: 2019年06月14日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public class MyFeatureTask<T> implements Runnable, Future<T> {

    private MCallable<T> callable;
    private T result;
    private Object notify;

    public MyFeatureTask(MCallable<T> callable) {
        this.callable = callable;
        notify=new Object();
    }

    @Override
    public void run() {
        T call = callable.call();
        result=call;
        //释放线程等待
        synchronized (notify){
            notify.notify();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        synchronized (notify){
        while (result==null){
                notify.wait();
            }
        return result;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
