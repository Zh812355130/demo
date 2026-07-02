package org.huan.demo.demo.designPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActiveObjectEx {
    private static final int NUM_CREATURES = 3;

    public static void main(String[] args) {
        /**
         * 活动对象模式的类将包含自同步机制，而无需使用“同步”方法
         * 扩展ActiveCreature的类都将具有自己的线程来执行和调用方法
         */
        List<ActiveCreature> creatures = new ArrayList<>();
        try {
            for (int i = 0; i < NUM_CREATURES; i++) {
                creatures.add(new Orc(Orc.class.getSimpleName() + i));
                creatures.get(i).eat();
                creatures.get(i).roam();
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            for (int i = 0; i < NUM_CREATURES; i++) {
                creatures.get(i).kill(0);
            }
        }
    }

}

abstract class ActiveCreature {
    private BlockingQueue<Runnable> requests;

    private String name;
    private Thread thread;

    private int status;

    public ActiveCreature(String name) {
        this.name = name;
        this.status = 0;
        this.requests = new LinkedBlockingQueue<>();
        thread = new Thread(() -> {
            boolean infinite = true;
            while (infinite) {
                try {
                    requests.take().run();
                } catch (InterruptedException e) {
                    if (this.status != 0) {
                        System.out.println("Thread was interrupted " + e.getMessage());
                    }
                    infinite = false;
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.start();
    }

    public void eat() throws InterruptedException {
        requests.put(new Runnable() {
            @Override
            public void run() {
                System.out.println(name() + " is eating!");
                System.out.println(name() + " has finishing eating!");
            }
        });
    }

    public void roam() throws InterruptedException {
        requests.put(new Runnable() {
            @Override
            public void run() {
                System.out.println(name() + " has started to roam and the wastelands.");
            }
        });
    }

    public String name() {
        return this.name;
    }

    public void kill(int status) {
        this.status = status;
        this.thread.interrupt();
    }

    public int getStatus() {
        return this.status;
    }

}

class Orc extends ActiveCreature {

    public Orc(String name) {
        super(name);
    }
}
