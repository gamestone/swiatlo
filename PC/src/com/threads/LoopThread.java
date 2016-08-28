package com.threads;

/**
 * Miłosz Ziernik
 * 2013/11/01 
 */
public abstract class LoopThread extends TThread {

    /**
     Metoda zwraca czas wstrzymania. Jeśli wartość jest mniejsza od 0,
     wątek zostanie przerwany
     */
    protected abstract void loop() throws Exception;

    protected boolean onBeforeExecute() {
        return true;
    }

    protected void onAfterExecute() {

    }
    /*
     protected boolean onException(Exception e) {
     return true;
     }
     */
    public int delay = 1000;
    public boolean constantInterval = false;

    @Override
    public void execute() throws Throwable {

        if (!onBeforeExecute())
            return;

        try {

            long time = System.currentTimeMillis();

            while (isRunning())
                try {

                    long sleep = delay;

                    if (constantInterval)
                        sleep = delay - (System.currentTimeMillis() - time);

                    if (sleep < 0)
                        sleep = 0;
                    sleep(sleep);

                    time = System.currentTimeMillis();

                    loop();

                } catch (InterruptedException ex) {
                    return;
                } catch (Throwable e) {
                    onException(e);
                }

        } finally {
            onAfterExecute();
        }
    }
}
