package com.didekin;

/**
 * User: pedro@didekin
 * Date: 09/08/16
 * Time: 10:48
 */
public class ThreadPoolConstants {

    // Threads parameters.
    private static final int CPU_NUMBER = Runtime.getRuntime().availableProcessors();
    private static final double TARGET_CPU_USE = 0.9;

    // Jetty threads.
    @SuppressWarnings("WeakerAccess")
    static final int MIN_THREADS_FRONT = 10;
    private static final double RATIO_WAIT_COMPUTE_FRONT = 2.5;
    private static final double RATIO_FRONT = 0.75;
    static final int IDLE_TIMEOUT_FRONT = 30000; /* milliseconds.*/
    // Formula from Goetz book.
    private static final int RULE_ONE = Math.toIntExact(Math.round(CPU_NUMBER * TARGET_CPU_USE * RATIO_FRONT * (1 + RATIO_WAIT_COMPUTE_FRONT)));
    static final int MAX_THREADS_FRONT = RULE_ONE > MIN_THREADS_FRONT ? RULE_ONE : MIN_THREADS_FRONT;


    // GCM threads
    /**
     * Timeout for shuttingdown gracefully the GCM executorServices. In seconds.
     */
    public static final int TERMINATION_TIMEOUT = 120;
    public static final long KEEP_ALIVE_MILLISEC = 500L;
    private static final double RATIO_WAIT_COMPUTE_GCM = 3;
    public static final int MAX_THREADS_GCM = Math.toIntExact(Math.round(CPU_NUMBER * TARGET_CPU_USE * (1 - RATIO_FRONT) * (1 + RATIO_WAIT_COMPUTE_GCM)));
}
