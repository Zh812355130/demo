package org.huan.demo.demo.designPatterns;

import lombok.Getter;

interface RemoteServiceInterface{
    long doRemoteFunction(int value);
}
interface RandomProvider{
    double random();
}

enum RemoteServiceStatus{
    FAILURE(-1);
    @Getter
    private final long remoteServiceStatusValue;
    RemoteServiceStatus(long remoteServiceStatusValue){
        this.remoteServiceStatusValue = remoteServiceStatusValue;
    }
}

class RemoteService implements RemoteServiceInterface{
    private static final int THRESHOLD = 200;

    //单例的远程服务
    private static RemoteService service = null;

    private final RandomProvider randomProvider;


    static synchronized RemoteService getRemoteService(){
        if(service == null){
            service = new RemoteService();
        }
        return service;
    }
    private RemoteService(){
        this(Math::random);
    }

    RemoteService(RandomProvider randomProvider){
        this.randomProvider = randomProvider;
    }

    @Override
    public long doRemoteFunction(int value) {
        long waitTime = (long) Math.floor(randomProvider.random()*1000);
        try {
            Thread.sleep(waitTime);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
        return waitTime <= THRESHOLD ? value* 10L : RemoteServiceStatus.FAILURE.getRemoteServiceStatusValue();
    }
}

class ServiceAmbassador implements RemoteServiceInterface{
    private static final int RETRIES = 3;
    private static final int DELAY_MS = 3000;

    ServiceAmbassador(){}

    @Override
    public long doRemoteFunction(int value) {
        return safeCall(value);
    }

    private long checkLatency(int value){
        long startTime = System.currentTimeMillis();
        long result  = RemoteService.getRemoteService().doRemoteFunction(value);
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("Time take(ms) : "+timeTaken);
        return  result;
    }

    private long safeCall(int value){
        int retries = 0;
        long result = RemoteServiceStatus.FAILURE.getRemoteServiceStatusValue();
        for (int i = 0; i < RETRIES; i++) {
            if(retries >= RETRIES){
                return RemoteServiceStatus.FAILURE.getRemoteServiceStatusValue();
            }
            if((result = checkLatency(value)) == RemoteServiceStatus.FAILURE.getRemoteServiceStatusValue()){
                System.out.println("Failed to reach remote:("+i+")");
                retries++;
                try {
                    Thread.sleep(DELAY_MS);
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                }
            }else{
                break;
            }
        }
        return result;
    }
}
class Client{
    private final ServiceAmbassador serviceAmbassador = new ServiceAmbassador();
    long useService(int value){
        long result = serviceAmbassador.doRemoteFunction(value);
        System.out.println("Service result : "+result);
        return result;
    }
}



public class AmbassadorEx {

    public static void main(String[] args) {
        /**
         * 使用“大使”模式，我们可以实现来自客户端的频率较低的轮询以及延迟检查和日志记录。
         */
        Client host1 = new Client();
        Client host2 = new Client();
        host1.useService(12);
        host1.useService(73);
    }
}
