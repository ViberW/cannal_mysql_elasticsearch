package com.veelur.sync.elasticsearch.event;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.star.sync.elasticsearch.event.CanalEvent;

/**
 * @author Veelur
 * @version 1.0
 */
public class VerDeleteCanalEvent extends CanalEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public VerDeleteCanalEvent(Entry source) {
        super(source);
    }
}
