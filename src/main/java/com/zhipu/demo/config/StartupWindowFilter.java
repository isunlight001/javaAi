package com.zhipu.demo.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class StartupWindowFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        return StartupWindow.isStartup() ? FilterReply.NEUTRAL : FilterReply.DENY;
    }
} 