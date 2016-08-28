package com.threads;

import java.util.*;

/**
 * Miłosz Ziernik
 * 2013/11/01 
 * @param <T> Dowolny obiekt
 */
public abstract class QueueThread<T extends Object> extends TThread {

    private final LinkedList<T> queue = new LinkedList<>();
    private final LinkedList<T> currentItems = new LinkedList<>();
    private T currentItem;
    public int minDelay = 0; // minimalne opóżnienie (ms)
    public int idleTime = 0;
    private long lastPeak = new Date().getTime();

    /**
     Metoda wywoływana w momencie gdy przez czas dłuższy niż %idleTime% nie 
     zostanie wywołana metoda add.
     %idleTime% musi być większy od 0.
     Metoda wywoływana jest cyklicznie co %idleTime% milisekund
     */
    public void onIdle() {

    }

    public QueueThread() {
        super();
        setName("QueueThread [" + getId() + "]");
    }

    public void add(T item) {
        synchronized (this) {
            queue.add(item);
            lastPeak = new Date().getTime();
            notify();
        }
        if (getState() == State.NEW)
            start();
    }

    protected abstract void processQueue(LinkedList<T> items) throws Throwable;

    protected void beforeRun() {
        // ------------------------- do przeciazenia ----------------------
    }

    public int getQueueSize() {
        int idx = currentItem != null ? currentItems.indexOf(currentItem) : 0;
        if (idx < 0)
            idx = 0;
        return queue.size() + (currentItems.size() - idx);
    }

    protected void setCurrent(T item) {
        currentItem = item;
    }

    @Override
    protected void execute() throws Throwable {

        beforeRun();

        long lastUpdate = System.currentTimeMillis();
        while (!isInterrupted())
            try {

                currentItems.clear();

                if (queue.isEmpty())
                    synchronized (this) {
                        if (idleTime > 0)
                            this.wait(idleTime);
                        else
                            this.wait();
                    }

                if (idleTime > 0 && new Date().getTime() - lastPeak > idleTime) {
                    lastPeak = new Date().getTime();
                    onIdle();
                }

                if (queue.isEmpty())
                    continue;

                if (minDelay > 0) {
                    long time = System.currentTimeMillis();

                    if (time - lastUpdate < minDelay)
                        try {
                            Thread.sleep(minDelay - (time - lastUpdate));
                        } catch (InterruptedException e) {
                            return;
                        }
                }

                synchronized (this) {
                    currentItems.addAll(queue);
                    queue.clear();
                }

                if (!isRunning() || currentItems.isEmpty())
                    continue;

                currentItem = null;
                processQueue(currentItems);
                lastUpdate = System.currentTimeMillis();

            } catch (InterruptedException e) {
                return;
            } catch (Throwable e) {
                onException(e);
            }
    }
}
