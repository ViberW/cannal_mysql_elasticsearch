package com.dadaabc.sync.elasticsearch.event;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.star.sync.elasticsearch.event.CanalEvent;

/**
 * @author Veelur
 * @version 1.0
 */
public class DadaDeleteCanalEvent extends CanalEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public DadaDeleteCanalEvent(Entry source) {
        super(source);
    }
}
