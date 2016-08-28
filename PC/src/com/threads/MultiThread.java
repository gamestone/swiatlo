package com.threads;

import java.lang.management.*;
import java.util.*;

/**
 * Miłosz Ziernik
 * 2013/11/01 
 */
public abstract class MultiThread<T> {

    public static enum QueueExceedAction {

        skip, // pomin dodawany element
        shift, // usun najstarszy element
        wait // czekaj na zwolnienie kolejki
    }

    public abstract void execute(T item) throws Throwable;
    private final int threadsCount;
    protected final LinkedList<T> items = new LinkedList<>();
    public int maxQueueSize = 100000;
    public QueueExceedAction queueExceedAction = QueueExceedAction.wait;

    public void onException(Throwable e) {
    }

    public class SingleThread extends TThread {

        @Override
        protected void execute() throws Throwable {
            try {
                try {
                    while (!isInterrupted()) {
                        T item;
                        synchronized (items) {
                            item = items.pollFirst();
                        }

                        if (item == null)
                            synchronized (items) {
                                items.wait();
                                continue;
                            }

                        if (isRunning() && item != null)
                            try {
                                MultiThread.this.execute(item);
                            } catch (InterruptedException ex) {
                                return;
                            } catch (Throwable ex) {
                                onException(ex);
                            }
                    }
                } catch (InterruptedException ex) {
                }
            } finally {
                interrupt();
            }
        }
    }
    private final List<SingleThread> threads = new LinkedList<>();

    public void add(T item) {
        synchronized (items) {
            if (items.size() == maxQueueSize)
                switch (queueExceedAction) {
                    case skip:
                        return;
                    case shift:
                        items.pollFirst();
                        break;
                    case wait:
                        try {
                            items.wait();
                        } catch (InterruptedException ex) {
                            return;
                        }
                        break;

                }
            items.add(item);
            items.notifyAll();
        }
    }

    public MultiThread(String name) {
        this.threadsCount = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class).getAvailableProcessors();
        for (int i = 0; i < threadsCount; i++) {
            SingleThread th = new SingleThread();
            th.setName(name + " [" + i + "]");
            threads.add(th);
        }
    }

    public MultiThread(String name, int threadsCount) {
        this.threadsCount = threadsCount;
        for (int i = 0; i < threadsCount; i++) {
            SingleThread th = new SingleThread();
            th.setName(name + " [" + i + "]");
            threads.add(th);
        }

    }

    public void start() {
        synchronized (threads) {
            for (SingleThread th : threads)
                th.start();
        }
    }

    public void stop(int wait) {
        for (SingleThread th : threads)
            th.interrupt();

        if (wait > 0)
            synchronized (threads) {
                try {
                    for (SingleThread th : threads)
                        th.join(wait);
                } catch (InterruptedException ex) {
                }
            }

    }

    public void startBlocking() throws InterruptedException {
        start();
        for (SingleThread th : threads)
            th.join();
    }
}
