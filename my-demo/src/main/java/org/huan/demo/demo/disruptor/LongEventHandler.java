package org.huan.demo.demo.disruptor;

import com.lmax.disruptor.EventHandler;

public class LongEventHandler implements EventHandler<LongEvent> {

    @Override
    public void onEvent(LongEvent longEvent, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("Event:"+longEvent+",sequence:"+sequence+",endOfBatch:"+endOfBatch);
    }
}
