package com.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import static com.io.XStream.NodeType.*;

public abstract class XStream {

    public class CompressData {

        public int level;
    }

    public class CryptData {

        public boolean CBC = false;
        public String IVKey;
        public String key;
    }
    public final XNode root;

    public XStream(Integer id, String name, Object value) throws IOException {
        root = new XNode(null, id, name, null, value);
    }

    /* Nagłówek:
     * 1-4 - type id
     * 5 - skompresowany
     * 6 - zaszyfrowany
     * 7 - zawiera dane (nie pusty)
     * 8 > rozmiar
     */
    public class XStreamNode extends XNode {

        private XStreamNode(XNode parent, Integer id, String name, InputStream stream,
                int partsCount, CompressData compression, CryptData encryption)
                throws IOException {
            super(parent, id, name, ntStream, null);

        }
    }

    public static class XNode implements Iterable {

        public final XNode parent;
        public final String name;
        public final Integer id;
        private byte[] data;
        public int length;
        //   public final Set<XFlag> flags = new HashSet<>();
        public final NodeType type;
        private final List<XNode> nodes = new LinkedList<>();

        private XNode(XNode parent, Integer id, String name, NodeType type, Object value)
                throws IOException {
            this.id = id;
            this.name = name;
            this.parent = parent;
            this.type = type != null ? type : setValue(value);
            if (parent != null)
                parent.nodes.add(this);
        }

        private NodeType setValue(Object value) throws IOException {

            if (value instanceof Boolean) {
                data = ByteBuffer.allocate(1).put((Boolean) value ? (byte) 1 : 0).array();
                return ntBool;
            }

            if (value instanceof Byte) {
                data = ByteBuffer.allocate(1).put((Byte) value).array();
                return ntByte;
            }

            if (value instanceof Short) {
                data = ByteBuffer.allocate(2).putShort((Short) value).array();
                return ntShort;
            }

            if (value instanceof Short) {
                data = ByteBuffer.allocate(2).putShort((Short) value).array();
                return ntShort;
            }

            if (value instanceof Integer) {
                data = ByteBuffer.allocate(4).putInt((Integer) value).array();
                return ntInt;
            }

            if (value instanceof Long) {
                data = ByteBuffer.allocate(8).putLong((Long) value).array();
                return ntLong;
            }

            if (value instanceof Float) {
                data = ByteBuffer.allocate(4).putFloat((Float) value).array();
                return ntFloat;
            }

            if (value instanceof Double) {
                data = ByteBuffer.allocate(8).putDouble((Double) value).array();
                return ntDouble;
            }

            if (value instanceof Date) {
                data = ByteBuffer.allocate(8).putLong(((Date) value).getTime()).array();
                return ntDate;
            }

            if (value instanceof UUID) {
                data = new byte[16];
                ByteBuffer bb = ByteBuffer.wrap(data);
                bb.putLong(((UUID) value).getMostSignificantBits());
                bb.putLong(((UUID) value).getLeastSignificantBits());
                return ntUID;
            }

            throw new IOException("Nieobsługiwany typ danych");
        }

        public LinkedList<XNode> nodes() {
            LinkedList<XNode> lst = new LinkedList<>();
            lst.addAll(nodes);
            return lst;
        }

        public void remove() {
            if (parent == null)
                return;
            parent.nodes.remove(this);
        }

        public XNode addNode(Integer id, String name, Object value) throws IOException {
            return new XNode(this, id, name, null, value);
        }

        @Override
        public Iterator iterator() {
            return nodes.iterator();
        }
    }

    public static enum XStreamFlags {

        sfCompressedSize(1, -1, "Compressed size"),
        sfEncryptedSize(2, -1, "Encrypted size"),
        sfMd5(3, 16, "Value MD5"),
        sfPartsCount(4, -1, "Parts count"),
        sfBegin(5, 0, "Begin of stream"),
        sfEnd(6, 0, "End of stream"),;
        //---------------------------
        public final byte id;
        public final int size;
        public final String name;

        private XStreamFlags(int id, int size, String name) {
            this.id = (byte) id;
            this.size = size;
            this.name = name;
        }
    }

    public static enum NodeType {
        //maksymalna wartość: 15

        ntUnknown(0, 0, "Nieznany"),
        ntNull(1, 0, "Null"),
        ntBool(2, 1, "Boolean"),
        ntByte(3, 1, "Byte"),
        ntShort(4, 2, "Short"),
        ntInt(5, 4, "Integer"),
        ntFloat(6, 4, "Float"),
        ntLong(7, 8, "Long"),
        ntDouble(8, 8, "Double"),
        ntDate(9, 8, "Date"),
        ntUID(10, 8, "UID"), // 8 bajtow
        ntDynamic(11, 0, "Dynamic Value"), // dyamiczna wartość całkowita
        ntString(12, 0, "String"),
        ntStream(13, 0, "Byte Stream"),
        // object(14, 0, "Serialized Object"),
        ntNode(20, 0, "Node"),
        ntNodeId(21, 0, "Node Id"),
        ntNodeName(22, 0, "Node Name"),
        ntNodeEnd(23, 0, "Node End"),
        ntKeyHash(29, 16, "Value MD5"),;
        //---------------------------
        public final byte id;
        public final int size;
        public final String name;

        private NodeType(int id, int size, String name) {
            this.id = (byte) id;
            this.size = size;
            this.name = name;
        }
    }
}
