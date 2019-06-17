package com.example.demo1.Thread2;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Title: MyRejectedExecutionHandler <br>
 * Description: MyRejectedExecutionHandler <br>
 * Date: 2019年06月12日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public class MyRejectedExecutionHandler {


    public final class CallerRunsPolicy implements Rejected{
        public CallerRunsPolicy() {
        }

        /**
         * 如果线程池未关闭用调用者线程执行任务
         * @param r 任务
         * @param executor 线程池
         */
        @Override
        public void rejectedExecution(Runnable r, ExecutorService executor) {
            if (!executor.isShutdown()) {
                r.run();
            }
        }
    }
    public final class AbortPolicy implements Rejected{
        public AbortPolicy() {
        }

        /**
         * 丢弃任务并抛出异常
         * @param r 任务
         * @param executor 线程池
         */
        @Override
        public void rejectedExecution(Runnable r, ExecutorService executor) {
            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " +
                    executor.toString());
        }
    }
    public final class DiscardPolicy implements Rejected{
        public DiscardPolicy() {
        }

        /**
         * 丢弃任务不抛异常
         * @param r 任务
         * @param executor 线程池
         */
        @Override
        public void rejectedExecution(Runnable r, ExecutorService executor) {

        }
    }
    public final class DiscardOldestPolicy implements Rejected{
        public DiscardOldestPolicy() {
        }

        /**
         * 丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
         * @param r 任务
         * @param executor 线程池
         */
        public void rejected(Runnable r, MyTreadPool executor) {
            executor.totalTask.incrementAndGet();
            if (!executor.isShutdown()) {
                executor.getQueue().poll();
                executor.execute(r);
            }
        }
        @Override
        public void rejectedExecution(Runnable r, ExecutorService executor) {
            rejected(r, (MyTreadPool) executor);
        }

    }


}
