package org.huan.demo.demo.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;

public class LongEventMain {

    public static void handleEvent(LongEvent event,long sequence,boolean endOfBatch){
        System.out.println("Event:"+event+",sequence:"+sequence+",endOfBatch:"+endOfBatch);
    }

    public static void translate(LongEvent event,long sequence,ByteBuffer buffer){
        event.set(buffer.getLong(0));
    }

    public static void main(String[] args) throws Exception {
        int bufferSize =1024;
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);
//        disruptor.handleEventsWith((event,sequence,endOfBatch)-> System.out.println("Event:"+event+",sequence:"+sequence+",endOfBatch:"+endOfBatch));
//        disruptor.handleEventsWith(LongEventMain::handleEvent);
        disruptor.handleEventsWith(new LongEventHandler());
        disruptor.start();

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l=0;true;l++){
            bb.putLong(0,l);
//            ringBuffer.publishEvent((event,sequence,buffer)->event.set(buffer.getLong(0)),bb);
            ringBuffer.publishEvent(LongEventMain::translate,bb);
            Thread.sleep(1000);
        }
    }
}
