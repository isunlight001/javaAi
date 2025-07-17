package com.zhipu.demo.config;

public class StartupWindow {
    private static volatile boolean startup = true;

    public static boolean isStartup() {
        return startup;
    }

    public static void close() {
        startup = false;
    }
} 