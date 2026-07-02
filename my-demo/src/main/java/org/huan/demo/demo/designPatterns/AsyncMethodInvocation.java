package org.huan.demo.demo.designPatterns;

import java.sql.SQLOutput;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

interface AsyncCallBack<T> {
    void onComplete(T value);

    void onError(Exception ex);
}

interface AsyncResult<T> {
    boolean isCompleted();

    T getValue() throws ExecutionException;

    void await() throws InterruptedException;
}

interface AsyncExecutor {
    <T> AsyncResult<T> startProcess(Callable<T> task);

    <T> AsyncResult<T> startProcess(Callable<T> task, AsyncCallBack<T> callBack);

    <T> T endProcess(AsyncResult<T> asyncResult) throws ExecutionException, InterruptedException;
}


class ThreadAsyncExecutor implements AsyncExecutor {
    private final AtomicInteger idx = new AtomicInteger(0);


    @Override
    public <T> AsyncResult<T> startProcess(Callable<T> task) {
        return startProcess(task, null);
    }

    @Override
    public <T> AsyncResult<T> startProcess(Callable<T> task, AsyncCallBack<T> callBack) {
        CompletableResult<T> result = new CompletableResult<>(callBack);
        new Thread(()->{
            try {
                result.setValue(task.call());
            }catch (Exception ex){
                result.setException(ex);
            }
        },"executor-"+idx.incrementAndGet()).start();
        return result;
    }

    @Override
    public <T> T endProcess(AsyncResult<T> asyncResult) throws ExecutionException, InterruptedException {
        if(!asyncResult.isCompleted()){
            asyncResult.await();
        }
        return asyncResult.getValue();
    }


    static class CompletableResult<T> implements AsyncResult<T> {
        static final int RUNNING = 1;
        static final int FAILED = 2;
        static final int COMPLETED = 3;
        final Object lock;
        final AsyncCallBack<T> callBack;
        volatile int state = RUNNING;
        T value;
        Exception exception;

        CompletableResult(AsyncCallBack<T> callBack) {
            this.lock = new Object();
            this.callBack = callBack;
        }

        boolean hasCallback(){
            return callBack != null;
        }

        void setValue(T value){
            this.value = value;
            this.state = COMPLETED;
            if(hasCallback()){
                callBack.onComplete(value);
            }
            synchronized (lock){
                lock.notifyAll();
            }
        }

        void setException(Exception exception){
            this.exception = exception;
            this.state = FAILED;
            if(hasCallback()){
                callBack.onError(exception);
            }
            synchronized (lock){
                lock.notifyAll();
            }
        }

        @Override
        public boolean isCompleted() {
            return state > RUNNING;
        }

        @Override
        public T getValue() throws ExecutionException {
            if(state == COMPLETED){
                return value;
            }else if(state == FAILED){
                throw new ExecutionException(exception);
            }else{
                throw new IllegalStateException("Execution not completed yet");
            }
        }

        @Override
        public void await() throws InterruptedException {
            synchronized (lock){
                while (!isCompleted()){
                    lock.wait();
                }
            }
        }
    }
}


public class AsyncMethodInvocation {
    private static final String ROCKET_LAUNCH_LOG_PATTERN = "Space rocket <%s> launched successfully";
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ThreadAsyncExecutor executor = new ThreadAsyncExecutor();
        AsyncResult<Integer> asyncResult1 = executor.startProcess(lazyval(10, 500));
        AsyncResult<String> asyncResult2 = executor.startProcess(lazyval("test", 300));
        AsyncResult<Long> asyncResult3 = executor.startProcess(lazyval(50L, 700));
        AsyncResult<Integer> asyncResult4 = executor.startProcess(lazyval(20, 400),callBack("Deploying lunar rover"));
        AsyncResult<String> asyncResult5 = executor.startProcess(lazyval("callback", 600),callBack("Deploying lunar rover"));

        Thread.sleep(350);
        System.out.println("Mission command is sipping coffee");

        Integer result1 = executor.endProcess(asyncResult1);
        String result2 = executor.endProcess(asyncResult2);
        Long result3 = executor.endProcess(asyncResult3);

        asyncResult4.await();
        asyncResult5.await();

        log(String.format(ROCKET_LAUNCH_LOG_PATTERN,result1));
        log(String.format(ROCKET_LAUNCH_LOG_PATTERN,result2));
        log(String.format(ROCKET_LAUNCH_LOG_PATTERN,result3));

    }

    private static <T> Callable<T> lazyval(T value,long delayMillis){
        return ()->{
            Thread.sleep(delayMillis);
           log(String.format(ROCKET_LAUNCH_LOG_PATTERN,value));
           return value;
        };
    }

    private static <T> AsyncCallBack<T> callBack(String name){
        return new AsyncCallBack<T>() {
            @Override
            public void onComplete(T value) {
                log(name+" <" +value+">");
            }

            @Override
            public void onError(Exception ex) {
                log(name+" failed:" +ex.getMessage());
            }
        };
    }

    private static void log(String msg){
        System.out.println(msg);
    }
}
