package org.huan.demo.demo.designPatterns;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ActorModelEx {
    public static void main(String[] args) throws InterruptedException {
        /**
         * 参与者模型是一种并发计算的数学模型，它将“参与者”视为并发计算的通用原语。
         */
        ActorSystem system = new ActorSystem();
        Actor srijan = new ExampleActor(system);
        Actor ansh = new ExampleActor2(system);
        system.startActor(srijan);
        system.startActor(ansh);
        ansh.send(new Message("Hello ansh", srijan.getActorId()));
        srijan.send(new Message("Hello srijan!", ansh.getActorId()));
        Thread.sleep(1000);
        srijan.stop();
        ansh.stop();
        system.shutdown();
    }
}


abstract class Actor implements Runnable {
    @Setter
    @Getter
    private String actorId;

    private final BlockingQueue<Message> mailbox = new LinkedBlockingQueue<>();
    private volatile boolean active = true;

    public void send(Message message) {
        mailbox.add(message);
    }

    public void stop() {
        active = false;
    }

    @Override
    public void run() {
        while (active) {
            Message message = null;
            try {
                message = mailbox.take();
                onReceive(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected abstract void onReceive(Message message);
}

@Data
class Message {
    private final String content;
    private final String senderId;
}

class ActorSystem {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, Actor> actorRegister = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public void startActor(Actor actor) {
        String actorId = "actor-" + idCounter.incrementAndGet();
        actor.setActorId(actorId);
        actorRegister.put(actorId, actor);
        executor.submit(actor);
    }

    public Actor getActorById(String actorId) {
        return actorRegister.get(actorId);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

}

class ExampleActor extends Actor {

    private final ActorSystem actorSystem;

    @Getter
    private final List<String> receivedMessages = new ArrayList<>();

    ExampleActor(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    protected void onReceive(Message message) {
        System.out.println(getActorId() + "received : " + message.getContent() + " from " + message.getSenderId());
        Actor sender = actorSystem.getActorById(message.getSenderId());
        if (sender != null && !message.getSenderId().equals(getActorId())) {
            sender.send(new Message(" I got you message", getActorId()));
        }
    }
}


class ExampleActor2 extends Actor {
    private final ActorSystem actorSystem;
    @Getter
    private final List<String> receivedMessage = new ArrayList<>();

    ExampleActor2(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    protected void onReceive(Message message) {
        receivedMessage.add(message.getContent());
        System.out.println(getActorId() + " received : " + message.getContent());
    }
}
