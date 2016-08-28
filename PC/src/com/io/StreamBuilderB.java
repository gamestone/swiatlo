package com.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Miłosz Ziernik
 */
public class StreamBuilderB extends ByteArrayOutputStream {

    public void writeIntInverse(int val) throws IOException {
        int i = Integer.reverseBytes(val);
        write(ByteBuffer.allocate(4).putInt(i).array());
    }

    public void writeStr(String str) throws IOException {
        if (str == null)
            str = "";
        byte[] buff = str.getBytes("ISO-8859-2");
        writeIntInverse(buff.length);
        write(buff);
    }

    public void writeStrUTF8(String str) throws IOException {
        if (str == null)
            str = "";
        byte[] buff = str.getBytes("UTF-8");
        writeIntInverse(buff.length);
        write(buff);
    }
}
