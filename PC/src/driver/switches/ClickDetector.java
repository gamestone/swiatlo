package driver.switches;

import com.threads.TThread;
import com.utils.*;
import driver.Main;
import driver.channels.Channel;
import java.util.*;
import mlogger.*;

/**
 * Miłosz Ziernik
 * 2014/08/03 
 */
public class ClickDetector implements Runnable {

    public final static int CLICK_TIME = Main.clietMode ? 500 : 300;

    private final static ClickDetector impl = new ClickDetector();

    private final static LinkedList<ClickEvent> list = new LinkedList<>();
    private final Thread thread = new Thread(this);

    public static ClickEvent onSwitchDown(Channel channel, Switch sw, boolean up, int initValue) {
        ClickEvent cev = null;

        synchronized (list) {

            for (ClickEvent c : list) {
                if (c.sw.equals(sw) && c.up == up) {
                    cev = c;
                    break;
                }
            }

            if (cev == null) {
                cev = new ClickEvent(channel, sw, up, initValue);
                list.add(cev);
            }

            cev.lastPeak = System.currentTimeMillis();
            cev.events.add(cev.lastPeak);
            list.notify();
        }

        if (impl.thread.getState() == Thread.State.NEW)
            impl.thread.start();

        return cev;
    }

    @Override
    public void run() {

        while (!thread.isInterrupted())
            try {

                if (list.isEmpty())
                    synchronized (list) {
                        list.wait();
                    }

                if (list.isEmpty())
                    continue;

                LinkedList<ClickEvent> lst = new LinkedList<>();
                synchronized (list) {
                    lst.addAll(list);
                }

                long time = System.currentTimeMillis();

                for (ClickEvent ce : lst) {
                    if (time - ce.lastPeak < (Main.clietMode ? 300 : 100)) {
                        continue;
                    }

                    if (time - ce.created < CLICK_TIME)
                        try {
                            ce.sw.onClickEvent(ce);
                        } catch (Throwable e) {
                            Log.error(e);
                        }
                    synchronized (list) {
                        list.remove(ce);
                    }
                }
                
                Thread.sleep(1);

            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                Log.error(e);
            }
    }

    public static class ClickEvent {

        public final Switch sw;
        public final Boolean up;
        public final Channel channel;
        public final long created = System.currentTimeMillis();
        public long lastPeak = System.currentTimeMillis();
        public final int initValue;
        public final List<Long> events = new LinkedList<Long>();

        public ClickEvent(Channel channel, Switch sw, Boolean up, int initValue) {
            this.sw = sw;
            this.up = up;
            this.initValue = initValue;
            this.channel = channel;
        }

    }

}
