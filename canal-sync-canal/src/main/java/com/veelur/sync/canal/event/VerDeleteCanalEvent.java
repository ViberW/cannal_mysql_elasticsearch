package com.veelur.sync.canal.event;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

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
