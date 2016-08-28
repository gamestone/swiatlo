package com.threads;

import java.util.*;
import java.util.logging.*;
import mlogger.*;

/**
 * Miłosz Ziernik
 * 2013/11/01 
 */
public abstract class TThread extends Thread implements Runnable {

    public int delay = 0;
    public final static List<TThread> allThreads = new LinkedList<>();

    public TThread() {
        super();
        setName("TThread [" + getId() + "]");
    }

    public void start(int delay) {
        this.delay = delay;
        start();
    }

    public static Logger logger() {
        return Logger.getLogger("mlogger");
    }

    protected abstract void execute() throws Throwable;

    protected void onTerminate(Throwable exception) {
    }

    protected void onException(Throwable e) throws Throwable {
        Log.error(e);
    }

    /**
     Podstawowa metoda sprawdzania czy można kontynuować iterację pętli
     wątku
     */
    protected boolean isRunning() {
        return isAlive() && !isInterrupted();
    }

    public void interupt(int wait) {
        super.interrupt();
        if (wait > 0)
            try {
                join(wait);
            } catch (InterruptedException ex) {
                return;
            }
    }

    @Override
    public final void run() {

        if (delay > 0)
            try {
                Thread.sleep(delay);

            } catch (InterruptedException ex) {
                return;
            }

        String tname = null;

        synchronized (allThreads) {
            allThreads.add(this);
        }
        Throwable err = null;
        try {
            try {
            /*    BaseContext.waitForContextInitialized();

                tname = getName();
                if (tname == null)
                    tname = "TThread " + getId();
                if (!tname.contains(Long.toString(getId())))
                    tname += " [" + getId() + "]";
                if (CDebug.threadEvents())
                    Log.event("Thread", "Start: " + tname,
                            "class: " + getClass().getName());
*/
                execute();
            } catch (InterruptedException ex) {
                err = ex;
                return;
            } catch (Throwable ex) {
                err = ex;
                try {
                    onException(ex);
                } catch (Throwable ee) {
                    err = ee;
                    return;
                }
            }
        } finally {
            onTerminate(err);
          /*  if (CDebug.threadEvents())
                Log.event("Thread", "Stop: " + tname,
                        "class: " + getClass().getName());*/
        }
        synchronized (allThreads) {
            allThreads.remove(this);
        }
    }

    public static void interuptAll(int timeout) throws InterruptedException {
        List<TThread> list = new LinkedList<>();
        synchronized (TThread.allThreads) {
            for (TThread th : TThread.allThreads) {
                list.add(th);
                th.interrupt();
            }
        }
        if (timeout > 0)
            for (TThread th : list)
                th.join(timeout / list.size());
    }
}
