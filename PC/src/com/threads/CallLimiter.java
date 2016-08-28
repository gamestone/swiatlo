package com.threads;

import java.util.*;

/**
 * Miłosz Ziernik
 * 2013/11/01 
 */
public abstract class CallLimiter<T> extends QueueThread<T> {
    
    public CallLimiter(int minDelay) {
        super();
        this.minDelay = minDelay;
    }
    
    public abstract void onCall(T item);
    
    @Override
    protected void processQueue(LinkedList<T> items) throws Throwable {
        onCall(items.peekLast());
    }
    
}
