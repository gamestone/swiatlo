package com;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ProgressListener {

    private long currentProgress;
    private long currentMax;
    private long totalProgress;
    private long totalMax;
    private String caption;
    private boolean breakProcess = false;
    private HashMap<String, Object> extra = new HashMap<>();
    private List<Exception> errors = new LinkedList<>();
    private int timeout;
    private Date lastUpdate;
    private long lastProgress;
    private float lastSpeed;
    private long lastSpeedTime;
    private String currentItem;

    public void onProgress() {
    }
    //------------------------------------------

    public ProgressListener(int timeout) {
        this.timeout = timeout;
    }

    public synchronized float getSpeed() {

        long diff = new Date().getTime() - lastSpeedTime;
        if (diff > 500) {
            double sp = (double) lastProgress / (double) diff;
            lastSpeed = (float) (sp * (float) 1000);
            lastSpeedTime = new Date().getTime();
            lastProgress = 0;
        }
        return lastSpeed;
    }

    public synchronized String getSpeedStr() {
        float f = getSpeed();
        String unt = " B/s";

        if (f > 1048576) {
            f = f / (float) 1048576;
            unt = " MB/s";
        } else
            if (f > 1024) {
                f = f / (float) 1024;
                unt = " KB/s";
            }

        f = (long) (f * 100) / (float) 100;
        return Float.toString(f) + unt;

    }

    public synchronized void setItemName(String name) {
        currentItem = name;
    }

    public synchronized String getItemName() {
        return currentItem;
    }

    public synchronized void setTimeOut(int milliseconds) {
        timeout = milliseconds;
    }

    public synchronized void resetTimeoutCounter() {
        lastUpdate = new Date();
    }

    public synchronized void putExtra(String key, Object value) {
        extra.put(key, value);
    }

    public synchronized Object getExtra(Object key) {
        return extra.get(key);
    }

    public synchronized void resetCurrent() {
        lastUpdate = new Date();
        caption = null;
        currentItem = null;
        currentMax = 0;
        currentProgress = 0;
        breakProcess = false;
        update();
    }

    public synchronized void resetAll() {
        totalMax = 0;
        totalProgress = 0;
        resetCurrent();
    }

    public synchronized void addError(Exception e) {
        errors.add(e);
        update();
    }

    public synchronized String[] getErrors() {
        String[] res = new String[errors.size()];

        for (int i = 0; i < errors.size(); i++)
            res[i] = Exceptions.exceptionToStr(errors.get(i), false);

        return res;

    }

    public synchronized boolean isCanceled() {
        return breakProcess;
    }

    public synchronized void cancel() {
        breakProcess = true;
    }

    public synchronized void setCurrent(long value) {
        currentProgress = value;
        update();
    }

    public synchronized void setCurrentMax(long value) {
        currentMax = value;
        update();
    }

    public synchronized void setTotal(long value) {
        lastProgress += (value - totalProgress);
        totalProgress = value;
        update();
    }

    public synchronized void setTotalMax(long value) {
        totalMax = value;
        update();
    }

    public synchronized void setCaption(String value) {
        caption = value;
        update();
    }

    public synchronized void incrCurrent(long value) {
        currentProgress += value;
        update();
    }

    public synchronized void incrCurrentMax(long value) {
        currentMax += value;
        update();
    }

    public synchronized void incrTotal(long value) {
        lastProgress += value;
        totalProgress += value;
        update();
    }

    public synchronized void incrTotalMax(long value) {
        totalMax += value;
        update();
    }

    public synchronized long getCurrent() {
        return currentProgress;
    }

    public synchronized long getCurrentMax() {
        return currentMax;
    }

    public synchronized float getCurrentFloat() {
        if (currentMax > 0)
            return (float) currentProgress / (float) currentMax;
        else
            return 0;
    }

    public synchronized long getTotal() {
        return totalProgress;
    }

    public synchronized long getTotalMax() {
        return totalMax;
    }

    public synchronized float getTotalFloat() {
        if (totalMax > 0)
            return (float) totalProgress / (float) totalMax;
        else
            return 0;
    }

    public synchronized String getCaption() {
        return caption != null ? caption : "";
    }

    private void update() {

        if (timeout <= 0 || lastUpdate == null)
            return;

        if (new Date().getTime() - lastUpdate.getTime() > timeout)
            cancel();

    }
    /*
     public void buildStatusHeader(Page page) {

     float curr = currentMax != 0 ? (float) currentProgress / (float) currentMax : 0;
     float tot = totalMax != 0 ? (float) totalProgress / (float) totalMax : 0;

     String s = Float.toString((int) (curr * 1000) / (float) 10) + "%, "
     + Utils.formatUnit(currentProgress) + " / "
     + Utils.formatUnit(currentMax);

     page.setCustomResponseHeader("current", Float.toString(curr));
     page.setCustomResponseHeader("current-str", s);

     //    if (multiFiles) {
     page.setCustomResponseHeader("total", Float.toString(tot));
     s = Float.toString((int) (tot * 1000) / (float) 10) + "%, "
     + Utils.formatUnit(totalProgress) + " / "
     + Utils.formatUnit(totalMax);
     page.setCustomResponseHeader("total-str", s);
     //  }

     page.setCustomResponseHeader("caption", caption);

     resetTimeoutCounter();
     Collection keys = extra.keySet();
     for (Object o : keys) {
     if (o instanceof String) {
     page.setCustomResponseHeader((String) o, extra.get(o).toString());
     }
     }

     page.setCustomResponseHeader("speed", getSpeedStr());
     page.setCustomResponseHeader("item", Utils.cutLongName(currentItem, 40, true));

     String err = "";
     for (Exception e : errors) {
     if (!err.isEmpty()) {
     err += "|$|";
     }
     err += Exceptions.exceptionToStr(e, false);
     }

     page.setCustomResponseHeader("errors", err);
     }
     */
}
