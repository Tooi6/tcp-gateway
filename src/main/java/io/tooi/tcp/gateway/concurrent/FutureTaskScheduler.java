package io.tooi.tcp.gateway.concurrent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tooi
 * @since 2021-02-14 16:05:37
 */
@Slf4j
public class FutureTaskScheduler extends Thread {
    private ConcurrentLinkedQueue<Runnable> executeTaskQueue =
            new ConcurrentLinkedQueue<Runnable>();// 任务队列
    private long sleepTime = 200;// 线程休眠时间
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    private static FutureTaskScheduler inst = new FutureTaskScheduler();

    private FutureTaskScheduler() {
        this.start();
    }

    /**
     * 添加任务
     *
     * @param executeTask
     */


    public static void add(Runnable executeTask) {
        inst.executeTaskQueue.add(executeTask);
    }

    @Override
    public void run() {
        while (true) {
            handleTask();// 处理任务
            threadSleep(sleepTime);
        }
    }

    private void threadSleep(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            log.error("线程等待异常:", e);
        }
    }

    /**
     * 处理任务队列，检查其中是否有任务
     */
    private void handleTask() {
        Runnable executeTask;
        while (executeTaskQueue.peek() != null) {
            executeTask = executeTaskQueue.poll();
            handleTask(executeTask);
        }
    }

    /**
     * 执行任务操作
     *
     * @param executeTask
     */
    private void handleTask(Runnable executeTask) {
        pool.execute(new ExecuteRunnable(executeTask));
    }

    class ExecuteRunnable implements java.lang.Runnable {
        Runnable executeTask;

        ExecuteRunnable(Runnable executeTask) {
            this.executeTask = executeTask;
        }

        public void run() {
            executeTask.run();
        }
    }
}
