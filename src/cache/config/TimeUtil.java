package com.example.demo1.cache.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Title: TimeUtil <br>
 * Description: TimeUtil <br>
 * Date: 2019年06月17日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
@Slf4j
public class TimeUtil {

    public static void task(List<Task> tasks) {
        ScheduledExecutorService scheduledExecutorService =new ScheduledThreadPoolExecutor(tasks.size()
                , new ThreadFactoryBuilder().setDaemon(false).setNameFormat("-pool-%d").build());

        tasks.forEach(task -> {
            scheduledExecutorService.scheduleAtFixedRate(task.runnable, task.initialDelay, task.period, task.timeUnit);
            log.info("{};{}",Thread.currentThread().getName(),task);
        });
    }

    /**
     * 构造任务
     */
    @Builder
    @Data
    public static class TaskBuilder{

        private static List<Task> list = new ArrayList<>();

        private String name;

        public   TaskBuilder add(long initialDelay, long period, TimeUnit timeUnit, Runnable runnable){
            list.add(new Task(initialDelay, period, timeUnit, runnable));

            return this;
        }
        public  TaskBuilder add( long period,  Runnable runnable){
            list.add(new Task( period, runnable));
            return this;
        }

        public void taskRun() {
            TimeUtil.task(list);
        }
    }
    public static class Task {

        public Task(long initialDelay, long period, TimeUnit timeUnit, Runnable runnable) {
            this.initialDelay = initialDelay;
            this.period = period;
            this.timeUnit = timeUnit;
            this.runnable = runnable;
        }


        public Task(long period, Runnable runnable) {
            this.period = period;
            this.runnable = runnable;
        }

        private long initialDelay = 0;

        private long period;

        private TimeUnit timeUnit = TimeUnit.SECONDS;

        private Runnable runnable;

    }
}
