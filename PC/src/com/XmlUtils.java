package com;

import com.XML_old.XmlException;
import com.XML_old.XmlNode;
import mlogger.Log;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Miłosz Ziernik
 * 2013/01/24 
 */
public class XmlUtils {

    public static interface IWriteValue {

        public String valueToText(Field field, Object value, String text);
    }

    /**
     Klasa serializuje strukturę obiektów do XML-a. Obsługiwane są listy.
    
     */
    public static class XmlRecord {

        // anotacja umożliwiająca dodawanie opisów dla pól
        @Target(value = {ElementType.FIELD})
        @Retention(value = RetentionPolicy.RUNTIME)
        public @interface Node {

            public String description(); // opis

            public boolean notNull() default false;
        }
        private XmlNode ___xmlNode;

        /**
         Zwraca gałąź po wywołaniu asNode 
         */
        public final XmlNode getNode() {
            return ___xmlNode;
        }

        public void save(XmlNode parent, boolean skipNullFields,
                IWriteValue writeIntf, boolean useAnnotations)
                throws XmlException {
            enumAsNode(parent, this, skipNullFields, writeIntf, useAnnotations);
        }

        public void load(XmlNode node) throws Exception {
            enumLoad(node, this);
        }

        @SuppressWarnings("unchecked")
        private static void enumLoad(XmlNode node, XmlRecord rec) throws Exception {
            rec.___xmlNode = node;

            Field[] fds = rec.getClass().getFields();
            for (Field f : fds) {
                int mod = f.getModifiers();

                if (Modifier.isAbstract(mod) || Modifier.isPrivate(mod))
                    continue;

                XmlNode nd = node.node(f.getName(), false);

                if (nd == null
                        || nd.getInnerText() == null
                        || nd.getInnerText().isEmpty()) {

                    Node annotation = f.getAnnotation(Node.class);

                    if (annotation != null && annotation.notNull())
                        throw new XmlException("Wartość \""
                                + f.getName() + "\" nie może być pusta");
                }

                if (nd == null) {
                    if (!Modifier.isFinal(mod))
                        try {
                            f.set(rec, null);
                        } catch (Exception e) {
                            Log.warning(e);
                            // proba ustawienia nulla
                        }
                    continue;
                }

                if (Utils.hasAsSuperClsss(XmlRecord.class, f.getType())) {
                    XmlRecord newRec = (XmlRecord) f.get(rec);
                    if (newRec == null && !Modifier.isFinal(mod)) {
                        newRec = (XmlRecord) f.getType().newInstance();
                        f.set(rec, newRec);
                    }
                    if (newRec == null)
                        continue;

                    enumLoad(nd, newRec);
                    continue;
                }

                if (Utils.hasAsSuperClsss(List.class, f.getType())) {
                    List newList = (List) f.get(rec);
                    if (newList == null && !Modifier.isFinal(mod)) {
                        newList = (List) f.getType().newInstance();
                        f.set(rec, newList);
                    }

                    if (newList == null)
                        continue;

                    Class<?> listElementClass = null;

                    if (f.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType tt
                                = (ParameterizedType) f.getGenericType();
                        Type[] types = tt.getActualTypeArguments();
                        if (types != null && types.length > 0)
                            listElementClass = (Class<?>) types[0];
                    }

                    if (listElementClass == null)
                        throw new InstantiationError("Nie można określić klasy elementu listy");

                    for (XmlNode ln : node.getNodes()) {
                        Object listObj = null;
                        String val = ln.getInnerText();

                        if (Utils.hasAsSuperClsss(XmlRecord.class, listElementClass)) {
                            listObj = listElementClass.newInstance();
                            enumLoad(ln, (XmlRecord) listObj);
                        } else
                            listObj = FieldUtils.newInstance(listElementClass, val);

                        if (listObj == null)
                            throw new InstantiationError("Nieobsługiwana klasa "
                                    + listElementClass.getSimpleName());
                        newList.add(listObj);
                    }
                }

                String val = nd.getInnerText();

                if (val != null)
                    FieldUtils.setFieldValue(f, rec, val);

            }
        }

        private static XmlNode enumAsNode(XmlNode node, XmlRecord rec,
                boolean skipNullFields, IWriteValue writeIntf, boolean useAnnotations)
                throws XmlException {

            rec.___xmlNode = node;

            Field[] fds = rec.getClass().getFields();
            for (Field f : fds) {
                int mod = f.getModifiers();

                if (Modifier.isAbstract(mod) || Modifier.isPrivate(mod))
                    continue;

                Object obj;
                try {
                    obj = f.get(rec);
                    if (obj == null && skipNullFields)
                        continue;
                } catch (Exception e) {
                    Log.warning(e);
                    continue;
                }

                Node annotation = f.getAnnotation(Node.class);
                if (useAnnotations && annotation != null && annotation.description() != null)
                    node.addComment(annotation.description());

                if (obj instanceof XmlRecord) {
                    enumAsNode(node.addNode(f.getName()),
                            (XmlRecord) obj, skipNullFields, writeIntf, useAnnotations);
                    continue;
                }

                if (obj instanceof List) {

                    List list = (List) obj;
                    for (Object lo : list) {

                        if (lo == null && skipNullFields)
                            continue;

                        if (lo instanceof XmlRecord) {
                            enumAsNode(node.addNode(f.getName()),
                                    (XmlRecord) lo, skipNullFields, writeIntf, useAnnotations);
                            continue;
                        }

                        node.addNode(f.getName()).setInnerText(lo.toString());
                    }
                }

                if (FieldUtils.isSimpleField(f, false))
                    try {
                        String val = FieldUtils.getFieldValue(f, rec);

                        if (writeIntf != null)
                            val = writeIntf.valueToText(f, obj, val);

                        node.addNode(f.getName()).setInnerText(val);
                    } catch (IllegalAccessException | IllegalArgumentException ex) {
                        Log.warning(ex);
                    }

            }
            return null;
        }
    }
}
