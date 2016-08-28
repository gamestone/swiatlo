package com.hashes;

import com.hashes.Hashes.Hash;
import com.hashes.Hashes.HashInputStream;
import com.io.TOutputStream;
import mlogger.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**

 @author Miłosz Ziernik
 */
public class Crypto {

    public static String encrypt(String message, String pass) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(Hashes.hashB(Hash.MD5, pass), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] buff = cipher.doFinal(message.getBytes());
            return Hex.toString(buff);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String decryptString(String message, String pass) {
        try {
            byte[] buff = Hex.toBytes(message);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(Hashes.hashB(Hash.MD5, pass), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(buff));

        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] Encrypt(byte[] buffer, String sKey)
            throws IOException {

        /*
         * Algorytm: Rijndael Klucz: 128 b Blok: 16 B Tryb: ECB (blokowy, nie
         * rekurencyjny) Dopełnienie: PKCS5
         *
         */
        byte pkcsValue = (byte) (16 - buffer.length % 16); // wartosc uzupelniajaca PKSC5
        byte[] pkcsBuffer = new byte[buffer.length % 16]; // bufor wartosci PKSC

        for (int n = 0; n < pkcsBuffer.length; n++) // wypelnij bufor jedna wartoscia
            pkcsBuffer[n] = pkcsValue;

        // haszowanie 128 bitowe - MD5
        byte[] bKey = null;
        try {
            bKey = MessageDigest.getInstance("MD5").digest(sKey.getBytes());
        } catch (NoSuchAlgorithmException ex) {
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("Rijndael/ECB/PKCS5Padding"); //
        } catch (NoSuchAlgorithmException ex) {
            return null;
        } catch (NoSuchPaddingException ex) {
            return null;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(bKey, "Rijndael"));
        } catch (InvalidKeyException ex) {
            return null;
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        CipherOutputStream cipherStream = new CipherOutputStream(bOut, cipher);

        cipherStream.write(buffer);
        cipherStream.write(pkcsBuffer); //dopisz uzupelnienie PKCS

        return bOut.toByteArray();
    }
    /*
     public static byte[] Decrypt_256_CBC(byte[] buffer, String sKey, int OryginalSize)
     throws IOException {

     Rijndael cipher = new Rijndael();
     try {
     cipher.makeKey(MessageDigest.getInstance("SHA-256").digest(sKey.getBytes()), 256);
     } catch (NoSuchAlgorithmException ex) {
     }
     ByteArrayOutputStream bOut = new ByteArrayOutputStream();

     byte[] block = new byte[Rijndael.BLOCK_SIZE];
     byte[] temp = new byte[Rijndael.BLOCK_SIZE];
     byte[] xor = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

     int offset = 0;
     int cnt = 0;
     int len = 0;

     while (offset < buffer.length) {

     cnt = buffer.length - offset;
     if (cnt > Rijndael.BLOCK_SIZE) {
     cnt = Rijndael.BLOCK_SIZE;
     } else {
     Arrays.fill(block, (byte) 0);
     }

     System.arraycopy(buffer, offset, block, 0, cnt);
     offset += block.length;

     byte[] buff = cipher.decryptBlock(block, temp);
     for (int n = 0; n < Rijndael.BLOCK_SIZE; n++) {
     buff[n] = (byte) (buff[n] ^ xor[n]);
     }

     System.arraycopy(block, 0, xor, 0, Rijndael.BLOCK_SIZE);

     if (buffer.length - cnt < Rijndael.BLOCK_SIZE) {
     byte[] pkcs = new byte[Rijndael.BLOCK_SIZE - (buffer.length - cnt)];
     System.arraycopy(buff, 0, pkcs, 0, pkcs.length);

     len = pkcs.length;
     while (len + bOut.size() > OryginalSize) {
     --len;
     }
     bOut.write(pkcs, 0, len);
     } else {
     len = buff.length;
     while (len + bOut.size() > OryginalSize) {
     --len;
     }
     bOut.write(buff, 0, len);
     }
     }
     return bOut.toByteArray();
     }
     */

    private static void writeInteger(ByteArrayOutputStream stream, int val) throws IOException {
        int i = Integer.reverseBytes(val);
        stream.write(ByteBuffer.allocate(4).putInt(i).array());
    }
    /*
     public static byte[] Encrypt_256_Multi(Byte threads, byte[] buffer, String sKey)
     throws IOException {

     int block_size = buffer.length / threads;

     ByteArrayOutputStream bOut = new ByteArrayOutputStream();
     while (block_size % 16 != 0) {
     block_size += 1;
     }

     int offset = 0;
     int count = 0;

     while (offset < buffer.length) {
     int v = block_size;
     if (offset + v > buffer.length) {
     v = buffer.length - offset;
     }

     byte[] buff = new byte[v];
     count += 1;
     System.arraycopy(buffer, offset, buff, 0, v);
     byte[] out_buff = EncryptECL(buff, sKey);

     bOut.write(count);   // zapisz numer czesci
     writeInteger(bOut, out_buff.length);// zapisz rozmiar czesci
     writeInteger(bOut, buff.length);         // zapisz oryginalny rozmiar
     bOut.write(out_buff);
     offset += out_buff.length;

     }

     ByteArrayOutputStream result = new ByteArrayOutputStream();
     result.write(count);    // zapisz ilosc czesci
     writeInteger(result, offset);    // zapisz laczny rozmiar


     bOut.writeTo(result);

     return result.toByteArray();
     }
     */

    public static class DecryptECLOutputStream extends OutputStream {

        private final OutputStream out;
        private final int oryginalSize;
        private final Rijndael cipher;
        private final byte[] block = new byte[Rijndael.BLOCK_SIZE];
        private final byte[] temp = new byte[Rijndael.BLOCK_SIZE];
        private final byte[] xor = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
        private byte[] buff = new byte[Rijndael.BLOCK_SIZE];
        private int pos = 0;
        private int blockPos = 0;
        public boolean enabled = true;

        public DecryptECLOutputStream(OutputStream out, String sKey, int oryginalSize) {
            this.out = out;
            this.oryginalSize = oryginalSize;

            cipher = new Rijndael();
            try {
                cipher.makeKey(MessageDigest.getInstance("SHA-256").digest(sKey.getBytes()), 256);
            } catch (NoSuchAlgorithmException ex) {
            }
        }

        public void writeBlock() throws IOException {

            buff = cipher.decryptBlock(block, temp);
            for (int n = 0; n < Rijndael.BLOCK_SIZE; n++)
                buff[n] = (byte) (buff[n] ^ xor[n]);

            System.arraycopy(block, 0, xor, 0, Rijndael.BLOCK_SIZE);

            if (oryginalSize > 0 && pos + buff.length > oryginalSize) {
                out.write(buff, 0, oryginalSize - pos);
                pos += oryginalSize - pos;
            } else {
                out.write(buff);
                pos += buff.length;
            }
        }

        @Override
        public void write(int b) throws IOException {
            if (!enabled) {
                out.write(b);
                return;
            }
            block[blockPos++] = (byte) b;
            if (blockPos >= Rijndael.BLOCK_SIZE) {
                blockPos = 0;
                writeBlock();
            }
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    public static class EncryptECLInputStream extends InputStream {

        private final InputStream in;
        private final Rijndael cipher;
        private final byte[] block = new byte[Rijndael.BLOCK_SIZE];
        private final byte[] temp = new byte[Rijndael.BLOCK_SIZE];
        private final byte[] xor = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
        private byte[] buff;
        private int pos = 0;
        private boolean eof = false;
        private int blockPos = 999999;
        public boolean enabled = true;
        //------------------
        public long time = 0;
        public int lastPos = 0;

        @Override
        public int available() throws IOException {
            int v = in.available();
            if (v <= 0)
                return v;
            return v + (Rijndael.BLOCK_SIZE - v % Rijndael.BLOCK_SIZE);
        }

        public EncryptECLInputStream(InputStream in, String sKey) {
            this.in = in;

            cipher = new Rijndael();
            try {
                cipher.makeKey(MessageDigest.getInstance("SHA-256").digest(sKey.getBytes()), 256);
            } catch (NoSuchAlgorithmException ex) {
            }
        }

        private boolean readBlock() throws IOException {
            int read = in.read(block);
            if (read <= 0)
                return false;

            // dopelnij ostatni blok
            for (int i = read; i < block.length; i++)
                block[i] = 0;

            for (int n = 0; n < Rijndael.BLOCK_SIZE; n++)
                block[n] = (byte) (block[n] ^ xor[n]);

            buff = cipher.encryptBlock(block, temp);
            System.arraycopy(buff, 0, xor, 0, Rijndael.BLOCK_SIZE);
            return read > 0;
        }

        @Override
        public int read() throws IOException {
            if (!enabled)
                return in.read();
            if (!eof && blockPos >= block.length) {
                blockPos = 0;
                if (!readBlock())
                    eof = true;
            }
            if (eof)
                return -1;
            ++pos;
            return 0xFF & buff[blockPos++];
        }
    }

    public static void EncryptECL(InputStream in, OutputStream out, String sKey) throws IOException {
        /*
         * Algorytm: Rijndael Klucz: 256 bitów Hasz SHA-256 Blok: 16 bajtów
         * Tryb: CBC (rekurencyjny XOR) Wektor IV: "InfoverSAeClicto"
         * Dopełnienie: brak Salt: brak
         *
         */
        Rijndael cipher = new Rijndael();
        try {
            cipher.makeKey(MessageDigest.getInstance("SHA-256").digest(sKey.getBytes()), 256);
        } catch (NoSuchAlgorithmException ex) {
        }
        byte[] block = new byte[Rijndael.BLOCK_SIZE];
        byte[] temp = new byte[Rijndael.BLOCK_SIZE];
        byte[] xor = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

        int read;
        Arrays.fill(block, (byte) 0);
        while ((read = in.read(block)) > 0) {
            // dopelnij ostatni blok
            for (int i = read; i < block.length; i++)
                block[i] = 0;

            for (int n = 0; n < Rijndael.BLOCK_SIZE; n++)
                block[n] = (byte) (block[n] ^ xor[n]);

            byte[] buff = cipher.encryptBlock(block, temp);
            System.arraycopy(buff, 0, xor, 0, Rijndael.BLOCK_SIZE);
            out.write(buff);
        }
    }

    /*
     public static byte[] EncryptECL(byte[] buffer, String sKey) throws IOException {
     /*
     * Algorytm: Rijndael Klucz: 256 bitów Hasz SHA-256 Blok: 16 bajtów
     * Tryb: CBC (rekurencyjny XOR) Wektor IV: "InfoverSAeClicto"
     * Dopełnienie: brak Salt: brak
     *
     */
    /*  Rijndael cipher = new Rijndael();
     try {
     cipher.makeKey(MessageDigest.getInstance("SHA-256").digest(sKey.getBytes()), 256);
     } catch (NoSuchAlgorithmException ex) {
     }
     ByteArrayOutputStream bOut = new ByteArrayOutputStream();

     byte[] block = new byte[Rijndael.BLOCK_SIZE];
     byte[] temp = new byte[Rijndael.BLOCK_SIZE];
     byte[] xor = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
     // byte[] xor = MessageDigest.getInstance("MD5").digest("Infover S.A. eClicto".getBytes());

     int offset = 0;
     int cnt = 0;

     while (offset < buffer.length) {

     cnt = buffer.length - offset;
     if (cnt > Rijndael.BLOCK_SIZE) {
     cnt = Rijndael.BLOCK_SIZE;
     } else {
     Arrays.fill(block, (byte) 0);
     }


     System.arraycopy(buffer, offset, block, 0, cnt);
     offset += block.length;

     for (int n = 0; n < Rijndael.BLOCK_SIZE; n++) {
     block[n] = (byte) (block[n] ^ xor[n]);
     }

     byte[] buff = cipher.encryptBlock(block, temp);

     System.arraycopy(buff, 0, xor, 0, Rijndael.BLOCK_SIZE);
     bOut.write(buff);
     }

     return bOut.toByteArray();
     }
     */
    public static byte[] Encrypt_256_ECB(byte[] buffer, String sKey) throws IOException, NoSuchAlgorithmException {

        Rijndael cipher = new Rijndael();
        cipher.makeKey(MessageDigest.getInstance("SHA-256").digest(sKey.getBytes()), 256);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        byte[] block = new byte[Rijndael.BLOCK_SIZE];
        byte[] temp = new byte[Rijndael.BLOCK_SIZE];

        int offset = 0;
        int cnt = 0;

        while (offset < buffer.length) {

            cnt = buffer.length - offset;
            if (cnt > Rijndael.BLOCK_SIZE)
                cnt = Rijndael.BLOCK_SIZE;
            else
                Arrays.fill(block, (byte) 0);

            System.arraycopy(buffer, offset, block, 0, cnt);
            offset += block.length;

            byte[] buff = cipher.encryptBlock(block, temp);
            bOut.write(buff);
        }
        return bOut.toByteArray();
    }

    /**
     MultipartWriter AES / 128 / ECB
     */
    public static class MultipartWriter {

        public byte[] signature = new byte[0];
        public byte[] extra = new byte[0];
        public int maxThreadsCount = 4;
        public boolean inverseByteOrder = false;
        public boolean savePasswordHash = true;
        public boolean savePartsMD5 = true;
        public boolean saveTotalMD5 = true;
        private byte[] bKey = new byte[0];

        public void run(String key, InputStream in, OutputStream out)
                throws IOException {
            bKey = Hashes.hashB(Hashes.Hash.MD5, key);

            int partsCount = in.available() / 100;
            if (partsCount <= 0)
                partsCount = 1;
            if (partsCount > maxThreadsCount)
                partsCount = maxThreadsCount;

            HashInputStream md5In = new HashInputStream(Hashes.Hash.MD5, in);
            if (saveTotalMD5)
                in = md5In;

            int partSize = (int) Math.floor((double) in.available() / (double) partsCount);
            while (partSize % 16 != 0)
                ++partSize;

            List<EncryptThread> lst = new LinkedList<>();
            for (int i = 0; i < partsCount; i++) {
                byte[] buff = i < partsCount - 1 ? new byte[partSize] : new byte[in.available()];
                in.read(buff);
                EncryptThread th = new EncryptThread();
                th.data.write(buff);
                lst.add(th);
                th.start();
            }
            // czekaj az wszystkie watki sie zakoncza
            for (EncryptThread th : lst)
                try {
                    th.join();
                } catch (InterruptedException ex) {
                    throw new IOException(ex);
                }

            byte[] bPassHash = savePasswordHash
                               ? Hashes.hashB(Hashes.Hash.MD5, bKey)
                               : new byte[0];

            if (savePasswordHash)
                for (int i = 0; i < bPassHash.length; i++)
                    bPassHash[i] = (byte) (bPassHash[i] ^ (~i - 0xA6));

            byte[] bTotalMD5 = saveTotalMD5 ? md5In.getAsBytes() : new byte[0];

            long partsSize = 0;
            for (EncryptThread th : lst)
                partsSize += th.buff.length;

            boolean extraData = extra.length > 0;

            byte flags = 0;
            flags |= inverseByteOrder ? 0x01 : 0;
            flags |= savePasswordHash ? 0x02 : 0;
            flags |= saveTotalMD5 ? 0x04 : 0;
            flags |= savePartsMD5 ? 0x08 : 0;
            flags |= extraData ? 0x10 : 0;

            TOutputStream sw = new TOutputStream(out);
            sw.setInverseByteOrder(inverseByteOrder);
            sw.writeByte((byte) signature.length);
            sw.write(signature);
            sw.writeByte(flags);
            sw.writeLong(partsSize);
            sw.writeByte((byte) lst.size());
            if (extraData) {
                sw.writeInt(extra.length);
                sw.write(extra);
            }
            sw.write(bPassHash);
            sw.write(bTotalMD5);

            for (EncryptThread th : lst)
                sw.write(th.buff);

            sw.flush();
        }

        private class EncryptThread extends Thread {

            public ByteArrayOutputStream data = new ByteArrayOutputStream();
            public byte[] buff = new byte[0];

            @Override
            public void run() {

                try {
                    int oryginalSize = data.size();

                    byte[] zeroBuffer = new byte[data.size() % 16 != 0
                                                 ? 16 - data.size() % 16 : 0];

                    byte[] md5 = savePartsMD5 ? Hashes.hashB(
                            Hashes.Hash.MD5, data.toByteArray()) : new byte[0];

                    Arrays.fill(zeroBuffer, (byte) 0);
                    data.write(zeroBuffer);

                    buff = data.toByteArray();
                    int totalSize = 4 + md5.length + buff.length;

                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    TOutputStream out = new TOutputStream(bout);
                    out.setInverseByteOrder(inverseByteOrder);
                    out.writeInt(totalSize);
                    out.writeInt(oryginalSize);
                    out.write(md5);

                    Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
                    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(bKey, "AES"));

                    try (CipherOutputStream cOut = new CipherOutputStream(out, cipher)) {
                        cOut.write(buff);
                        cOut.flush();
                    }
                    out.flush();
                    buff = bout.toByteArray();
                } catch (Exception ex) {
                    Log.error(ex);
                    buff = null;
                }
            }
        }
    }
}
