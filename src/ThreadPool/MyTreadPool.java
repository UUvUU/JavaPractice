package com.example.demo1.Thread2;

import com.example.demo1.Thread.CustomThreadPool;
import com.example.demo1.Thread.Notify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Title: MyTreadPool <br>
 * Description: MyTreadPool <br>
 * Date: 2019年06月12日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public class MyTreadPool extends AbstractExecutorService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MyTreadPool.class);
    /**
     * 锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Wait condition to support awaitTermination
     */
    private final Condition termination = lock.newCondition();

    /**
     * 核心线程数
     */
    private int coreSize;

    /**
     * 最大线程数
     */
    private int maxCoreSize;

    /**
     * 线程需要被回收的时间
     */
    private long keepAliveTime;
    private TimeUnit unit;

    /**
     * 存放线程的阻塞队列
     */
    private BlockingQueue<Runnable> workQueue;

    /**
     * 线程都满了后拒绝策略
     */
    private Rejected rejectedExecution;

    /**
     * 存放线程池
     */
    private volatile Set<Worker> workers;


    /**
     * 是否关闭线程池标志
     */
    private AtomicBoolean isShutDown = new AtomicBoolean(false);

    /**
     * 提交到线程池中的任务总数
     */
    protected AtomicInteger totalTask = new AtomicInteger();

    /**
     * 线程池任务全部执行完毕后的通知组件
     */
    private Object shutDownNotify = new Object();

    private Notify notify;

    /**
     * 通常执行
     * @param coreSize 核心线程个数
     * @param maxCoreSize 最大线程个数
     * @param keepAliveTime 存活时间
     * @param unit 单位
     * @param workQueue 存放线程队列
     * @param rejected 拒绝策略
     */
    public MyTreadPool(int coreSize, int maxCoreSize, long keepAliveTime, TimeUnit unit,
                       BlockingQueue<Runnable> workQueue, Rejected rejected) {
        this.coreSize = coreSize;
        this.maxCoreSize = maxCoreSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.rejectedExecution=rejected;
        /**
         * 初始化工作线程队列
         */
        workers = new ConcurrentHashSet<>();
    }

    /**
     * 结束回调
     * @param coreSize 核心线程个数
     * @param maxCoreSize 最大线程个数
     * @param keepAliveTime 存活时间
     * @param unit 单位
     * @param workQueue 存放线程队列
     * @param rejected 拒绝策略
     * @param notify 结束回调策略
     */
    public MyTreadPool(int coreSize, int maxCoreSize, long keepAliveTime, TimeUnit unit,
                       BlockingQueue<Runnable> workQueue, Rejected rejected, Notify notify) {
        this.coreSize = coreSize;
        this.maxCoreSize = maxCoreSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
        this.workQueue = workQueue;
        this.rejectedExecution=rejected;
        /**
         * 执行任务完操作
         */
        this.notify=notify;
        /**
         * 初始化工作线程队列
         */
        workers = new ConcurrentHashSet<>();
    }

    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }


    public <T> MyFeatureTask<T> submit(MCallable<T> callable) {
        MyFeatureTask<T> future = new MyFeatureTask(callable);
        execute(future);
        return future;
    }


    /**
     * 执行任务
     *
     * @param task 任务
     */
    @Override
    public void execute(Runnable task) {

        if (task == null) {
            throw new NullPointerException("任务不能为空");
        }
        if (isShutDown.get()) {
            LOGGER.info("正在关闭线程，不接受新任务");
            return;
        }


        totalTask.incrementAndGet();
        if (workers.size() < coreSize) {
            addWorker(task);
            return;
        }
        //加入队列失败
        if (!workQueue.offer(task)) {
            //是否大于最大线程
            if (workers.size() < maxCoreSize) {
                addWorker(task);
                return;
            }
            LOGGER.info("超过最大线程数");
            //拒绝策略
            totalTask.decrementAndGet();
            reject(task);
            //workQueue.put(runnable); 阻塞添加
        }


    }

    /**
     * 添加工作线程
     * @param task 任务
     */
    private void addWorker(Runnable task) {
        //创建新线程并添加当前任务
        Worker worker = new Worker(task, true);
        //开启线程执行
        worker.startTask();
        //加入工作线程队列
        workers.add(worker);
    }

    /**
     * ture 当任务全部执行完关闭线程
     * false 直接关闭所有线程
     *
     * @param b
     */
    private void tryClose(boolean b) {
        if (b) {
            if (totalTask.get() == 0 && isShutDown.get()) {
                closeAllThread();
            }
        } else {
            closeAllThread();
        }
    }

    /**
     * 关闭所有线程
     */
    private void closeAllThread() {
        for (Worker t : workers) {
            t.close();
        }
        isShutDown.set(true);
    }

    /**
     * 线程池是否关闭
     *
     * @return
     */
    public boolean poolIsClosed() {
        return isShutDown.get() && workers.size() == 0;
    }

    /**
     * 任务执行完关闭线程
     */
    @Override
    public void shutdown() {
        isShutDown.set(true);
            tryClose(true);
    }

    public void ssss(){
        isShutDown.set(true);
        tryClose(false);
    }
    /**
     * 立刻关闭所有线程，中断任务
     */
    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks = new ArrayList<>();
        final ReentrantLock mainLock = this.lock;
        mainLock.lock();
        try {
            isShutDown.set(true);
            tryClose(false);
            //未执行的任务
            BlockingQueue<Runnable> q = workQueue;
            q.drainTo(tasks);
            if (!q.isEmpty()) {
                for (Runnable r : q.toArray(new Runnable[0])) {
                    if (q.remove(r)) {
                        tasks.add(r);
                    }
                }
            }
        } finally {
            mainLock.unlock();
        }
        return tasks;
    }

    @Override
    public boolean isShutdown() {
        return isShutDown.get();
    }

    /**
     * 查看线程池是否已关闭
     *
     * @return
     */
    @Override
    public boolean isTerminated() {
        return poolIsClosed();
    }

    final void reject(Runnable command) {
        rejectedExecution.rejectedExecution(command, this);
    }

    /**
     * 等待单位时间后查看线程池是否已关闭
     *
     * @param timeout 时间
     * @param unit    单位
     * @return 是否关闭
     * @throws InterruptedException
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.lock;
        mainLock.lock();
        try {
            while (true) {
                if (poolIsClosed())
                    return true;
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 工作线程
     */
    private final class Worker extends Thread {
        /**
         * 任务
         */
        private Runnable task;
        /**
         * 线程本身
         */
        private Thread thread;
        /**
         * true --> 创建新的线程执行
         * false --> 从队列里获取线程执行
         */
        private boolean isNewTask;

        public Worker(Runnable task, boolean isNewTask) {
            this.task = task;
            this.thread = this;
            this.isNewTask = isNewTask;
        }

        public void startTask() {
            thread.start();
        }

        public void close() {
            thread.interrupt();
        }

        @Override
        public void run() {
            Runnable task = null;
            if (isNewTask) {
                task = this.task;
            }
            boolean flag = true;

            try {
                //判断当前线程没有任务并且获取不到任务时候进行回收
                //能获取到任务就执行  核心线程getTask会一直存活。非核心线程存活时未获取到任务则销毁
                while (task != null || (task = getTask()) != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        //出现异常 中断处理
                        flag = false;
                        throw e;
                    } finally {
                        task = null;
                        int number = totalTask.decrementAndGet();
                        //所以线程执行完回调函数
                        if (number == 0) {
                            if(notify!=null){
                                synchronized (notify) {
                                    notify.notifyListen();
                                }
                            }

                        }
                    }
                }
            } finally {
                //释放线程
                workers.remove(this);
                //线程出现异常抛出终止，新建一个空的工作线程用于获取新任务。
                if (!flag) {
                    addWorker(null);
                }
                //尝试关闭线程
                tryClose(true);
            }
        }
    }


    private Runnable getTask() {
        //关闭标识及任务是否全部完成
        if (isShutDown.get() && totalTask.get() == 0) {
            return null;
        }
        //加锁
        lock.lock();
        try {
            Runnable task;
            //大于核心线程数时需要用保活时间获取任务
            // 非核心线程等待一段时间没有任务就返还NUll 触发回收线程
            // 核心线程可以一直等待到有任务不会被回收
            if (workers.size() > coreSize) {
                task = workQueue.poll(keepAliveTime, unit);
            } else {
                task = workQueue.take();
            }

            if (task != null) {
                return task;
            }
        } catch (Exception e) {
            return null;
        } finally {
            lock.unlock();
        }

        return null;
    }



    /**
     * 内部存放工作线程容器，并发安全。
     *
     * @param <T>
     */
    private final class ConcurrentHashSet<T> extends AbstractSet<T> {
        /**
         * 通过ConcurrentHashMap实现线程安全的set
         */
        private ConcurrentHashMap<T, Object> map = new ConcurrentHashMap<>();
        private final Object PRESENT = new Object();

        private AtomicInteger count = new AtomicInteger();

        @Override
        public boolean add(T t) {
            count.incrementAndGet();
            return map.put(t, PRESENT) == null;
        }

        @Override
        public boolean remove(Object o) {
            count.decrementAndGet();
            return map.remove(o) == null;
        }

        @Override
        public Iterator<T> iterator() {
            return map.keySet().iterator();
        }

        /**
         * map的size不确定，用原子int计数
         */
        @Override
        public int size() {
            return count.get();
        }
    }


}
