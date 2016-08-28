package com.io;

import com.Utils;
import mlogger.Log;
import java.io.*;
import java.util.*;

public class NullInputStream extends InputStream implements Closeable {

    private final int length;
    private final Random random;
    private int cnt = 0;
    public boolean printSpeed = false;

    @Override
    public void reset() {
        cnt = 0;
    }

    public NullInputStream(int length, boolean randomBytes) {
        this.length = length;
        random = randomBytes ? new Random() : null;
    }
    private long time = 0;
    private int lastPos = 0;

    @Override
    public int read() throws IOException {
        cnt++;
        if (cnt > length)
            return -1;

        if (printSpeed) {

            Date date = new Date();
            if (date.getTime() - time >= 1000) {
                Log.debug("encrypt", Utils.formatFileSize(cnt)
                        + ", " + Utils.formatFileSize(cnt - lastPos) + "/s", null);
                time = date.getTime();
                lastPos = cnt;
            }
        }
        return random != null ? (byte) (0xFF & random.nextInt()) : 0;
    }
}
