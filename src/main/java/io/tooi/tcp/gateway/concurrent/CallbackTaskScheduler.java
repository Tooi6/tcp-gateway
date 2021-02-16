package io.tooi.tcp.gateway.concurrent;

import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tooi
 * @since 2021-02-14 18:51:05
 */
@Slf4j
public class CallbackTaskScheduler extends Thread {
    private ConcurrentLinkedQueue<CallbackTask> executeTaskQueue =
            new ConcurrentLinkedQueue<CallbackTask>();// 任务队列
    private long sleepTime = 200;// 线程休眠时间
    private ExecutorService jPool =
            Executors.newCachedThreadPool();

    ListeningExecutorService gPool =
            MoreExecutors.listeningDecorator(jPool);


    private static CallbackTaskScheduler inst = new CallbackTaskScheduler();

    private CallbackTaskScheduler() {
        this.start();
    }

    /**
     * 添加任务
     *
     * @param executeTask
     */


    public static <R> void add(CallbackTask<R> executeTask) {
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

        CallbackTask executeTask = null;
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
    private <R> void handleTask(CallbackTask<R> executeTask) {

        ListenableFuture<R> future = gPool.submit(new Callable<R>() {
            public R call() throws Exception {

                R r = executeTask.execute();
                return r;
            }

        });

        Futures.addCallback(future, new FutureCallback<R>() {
            public void onSuccess(R r) {
                executeTask.onBack(r);
            }

            public void onFailure(Throwable t) {
                executeTask.onException(t);
            }
        });


    }

}
