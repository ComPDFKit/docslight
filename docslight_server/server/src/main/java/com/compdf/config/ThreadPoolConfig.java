package com.compdf.config;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ComPDFKit-WPH 2025/3/7 0007
 */
public class ThreadPoolConfig {

    public static final ThreadPoolExecutor TASK_INIT_POOL = new ThreadPoolExecutor(
            1,
            10,
            60,
            TimeUnit.SECONDS,
            new java.util.concurrent.ArrayBlockingQueue<>(10),
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()
    );

}
