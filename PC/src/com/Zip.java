package com;

import com.FileUtils.FileParam;
import java.io.*;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;

public class Zip {

    private final static Object zipFsSync = new Object();

    /**
     * Dodaj do istniejacego archiwum zip plik (poprzez FileSystems)
     * @param file
     * @param name
     * @param data
     * @throws IOException 
     */
    public static void append(File file, String name, byte[] data) throws IOException {
        synchronized (zipFsSync) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            java.nio.file.Path path = Paths.get(file.toString());
            URI uri = URI.create("jar:" + path.toUri());
            try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
                java.nio.file.Path nf = fs.getPath(name);
                try (OutputStream out = Files.newOutputStream(nf, StandardOpenOption.CREATE);) {
                    out.write(data);
                }
            }
        }
    }

    /*
     * Metoda otwiera plik zip (jeśli istnieje) i dodaje nowy wpis
     */
    public static void append(File zipFile, byte[] entry, String entryName,
            boolean checkLast, boolean addTimeStamp, int compressionLevel, int maxEntries,
            String fileComment, String archiveCommet) throws IOException {

        // checkLast: jesli ostatni plik w archiwum jest identyczny jak bieżący to pomiń backup
        Zip zip = new Zip();
        if (zipFile.exists())
            try (BufferedInputStream in = new BufferedInputStream(
                    new FileInputStream(zipFile), 1024 * 100)) {
                zip = new Zip(in);

                if (checkLast && !zip.files.isEmpty()) {
                    ZipFile zf = zip.files.get(zip.files.size() - 1);
                    if (Arrays.equals(entry, zf.buffer))
                        return;
                }
            }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        String name = entryName;
        if (addTimeStamp)
            name = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + name;

        while (maxEntries > 0 && zip.files.size() > maxEntries)
            zip.files.remove(0);

        zip.addFile(FileUtils.formatFileName(name), entry, true).comment = fileComment;
        zip.compress(bout, compressionLevel, archiveCommet);

        FileUtils.save(bout.toByteArray(), zipFile);

    }

    public static class ZipFile {

        public String name;
        public byte[] buffer;
        public String comment;
        public long compressedSize;
        public long size;
        public long crc;
        public byte[] extra;
        public long time;
        public boolean directory;
        public boolean uncompressed = false;

        @Override
        public String toString() {
            return name + ", " + Utils.formatFileSize(size);
        }
    }
    public List<ZipFile> files = new LinkedList<>();
    public List<String> directories = new LinkedList<>();

    public ZipFile getFile(String fileName) {
        for (ZipFile zf : files)
            if (zf.name.equalsIgnoreCase(fileName))
                return zf;
        return null;
    }

    public String getUniquename(String name) {
        String s = null;
        int idx = 1;

        for (;;) {
            s = name;
            if (idx > 1) {
                String ext = "";
                if (name.indexOf(".") > 0) {
                    ext = name.substring(name.lastIndexOf("."), name.length());
                    name = name.substring(0, name.lastIndexOf("."));
                }
                s = name + " (" + idx + ")" + ext;
            }

            for (ZipFile zf : files)
                if (zf.name.equalsIgnoreCase(s)) {
                    s = null;
                    break;
                }

            if (s != null)
                return s;
            ++idx;
        }
    }

    public ZipFile addFile(File file, File rootPath, boolean uniqueName) throws IOException {

        String sroot = "";
        String sname = file.getAbsolutePath();
        if (rootPath != null)
            sroot = rootPath.getAbsolutePath();

        if (sname.startsWith(sroot))
            sname = sname.substring(sroot.length(), sname.length());

        sname = sname.replace("\\", "/");
        if (sname.startsWith("/"))
            sname = sname.substring(1, sname.length());

        if (uniqueName)
            sname = getUniquename(sname);

        byte[] buffer = FileUtils.load(file);

        return addFile(sname, buffer, uniqueName);
    }
    /*
     public void addFiles(File rootPath, boolean includeSubdirs) throws IOException {
       
     File[] files;
     if (includeSubdirs) {
     files = FileUtils.getFileList(rootPath);
     } else {
     files = rootPath.listFiles();
     }
     for (File f : files) {
     if (f.isFile()) {
     addFile(f, rootPath, false);
     }
     }
         
     }
     */

    public ZipFile addFile(String name, byte[] buffer, boolean uniqueName) {
        ZipFile file = null;
        if (uniqueName)
            name = getUniquename(name);
        else
            for (ZipFile zz : files)
                if (zz.name.equalsIgnoreCase(name)) {
                    file = zz;
                    break;
                }
        if (file == null) {
            file = new ZipFile();
            files.add(file);
        }
        file.name = name;
        file.buffer = buffer;
        return file;
    }

    public void compress(OutputStream out, int level, String comment) throws IOException {
        //level 0..9

        ZipOutputStream zos = new ZipOutputStream(out);
        try {
            zos.setLevel(level);
            zos.setComment(comment);
            for (ZipFile file : files) {
                ZipEntry ze = new ZipEntry(file.name);
                if (file.uncompressed) {
                    ze.setMethod(ZipEntry.STORED);
                    ze.setCompressedSize(file.buffer.length);
                    CRC32 crc = new CRC32();
                    crc.update(file.buffer);
                    ze.setCrc(crc.getValue());
                }
                ze.setSize(file.buffer.length);
                ze.setComment(file.comment);
                ze.setExtra(file.extra);
                zos.putNextEntry(ze);
                zos.write(file.buffer);
                zos.closeEntry();
            }

        } finally {
            zos.close();
        }
    }

    public Zip() {
    }

    public Zip(InputStream is) throws IOException {
        ZipInputStream zis = new ZipInputStream(is);
        try {
            ZipEntry entry;
            boolean correct = false;

            while ((entry = zis.getNextEntry()) != null) {
                correct = true;
                if (entry.isDirectory()) {
                    String ss = entry.getName();
                    directories.add(ss);
                    continue;
                }

                ZipFile zf = new ZipFile();
                files.add(zf);
                zf.name = entry.getName();
                zf.comment = entry.getComment();
                zf.compressedSize = entry.getCompressedSize();
                zf.size = entry.getSize();
                zf.crc = entry.getCrc();
                zf.extra = entry.getExtra();
                zf.time = entry.getTime();
                zf.directory = entry.isDirectory();

                ByteArrayOutputStream bout = new ByteArrayOutputStream();

                int BUFFER = 10248;
                byte data[] = new byte[BUFFER];
                int currentByte;

                while ((currentByte = zis.read(data, 0, BUFFER)) != -1)
                    bout.write(data, 0, currentByte);
                zf.buffer = bout.toByteArray();
            }
            if (!correct)
                throw new ZipException("Nieprawidłowy format archiwum");
        } catch (IllegalArgumentException e) {
            throw new ZipException("Nieprawidłowa struktura pliku ZIP");
        } finally {
            zis.close();
        }
    }

    public void saveFiles(String outDir) throws IOException {
        File ff = new File(outDir);
        ff.delete();
        for (ZipFile zf : files) {
            ff = new File(outDir, zf.name);
            ff.getParentFile().mkdirs();
            FileUtils.save(zf.buffer, ff);
        }
    }
    /*
     public static void zipFile(File file, File outFile, int compressionLevel)
     throws IOException {
     File[] files = new File[1];
     files[0] = file;
     zipFiles(files, outFile, compressionLevel);
     }
     */

    public static void ZipFies(Collection<String> sFiles, String rootPath, Boolean includeSubDirs,
            OutputStream outStream, int compressionLevel, ProgressListener progress)
            throws IOException, Exception {

        if (progress != null)
            progress.resetAll();

        byte[] buf = new byte[102400];

        String sRoot = rootPath;
        if (sRoot == null)
            sRoot = "";

        sRoot = sRoot.replace("\\", "/");

        if (sRoot.length() > 0 && !sRoot.endsWith("/") && !sRoot.endsWith("\\"))
            sRoot += "/";

        ZipOutputStream out = new ZipOutputStream(outStream);
        try {
            out.setLevel(compressionLevel);
            List<FileParam> files = FileUtils.createFileList(sFiles, includeSubDirs, progress);

            for (int i = 0; i < files.size(); i++) {
                if (progress != null && progress.isCanceled())
                    break;

                FileParam f = files.get(i);
                FileInputStream in = new FileInputStream(f.path.toString());

                String sName = f.path.toString().replace("\\", "/");

                if (sName.startsWith(sRoot))
                    sName = sName.substring(sRoot.length(), sName.length());

                sName = sName.replaceAll(":", "");
                sName = sName.replace("\\", "/");

                out.putNextEntry(new ZipEntry(sName));

                if (progress != null) {

                    progress.setCaption("[" + Integer.toString(i + 1) + " / "
                            + Integer.toString(files.size()) + "] " + f.getName());
                    progress.setCurrentMax(f.size);
                    progress.setCurrent(0);
                }

                int len;
                while ((len = in.read(buf)) > 0) {
                    if (progress != null) {
                        if (progress.isCanceled())
                            break;
                        progress.incrCurrent(len);
                        progress.incrTotal(len);

                        /*
                         if (outStream instanceof CacheOutputStream) {
                         long m = progress.getTotalMax();
                         if (m > 0) {
                         float ff = 1
                         - (float) ((CacheOutputStream) outStream).writedBytes
                         / (float) progress.getTotal();

                         ff = Math.round(ff * (float) 10000) / (float) 100;
                         progress.putExtra("compression", Float.toString(ff) + "%");
                         }

                         }
                         */
                    }
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        } finally {
            out.finish();
        }
    }

    /* public static void zipFiles(File[] files, File outFile, int compressionLevel)
     throws IOException {


     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
     ZipOutputStream zout = new ZipOutputStream(out);
     try {
     zout.setLevel(compressionLevel);
     for (File file : files) {


     FileInputStream fis = new FileInputStream(file);
     try {
     ZipEntry ze = new ZipEntry(file.getName());
     zout.putNextEntry(ze);
     byte[] buff = new byte[102400];

     int length;
     while ((length = fis.read(buff)) > 0) {
     zout.write(buff, 0, length);
     }

     zout.closeEntry();
     } finally {
     fis.close();
     }
     }
     } finally {
     zout.finish();
     zout.flush();
     out.flush();
     out.close();
     }
     }
     */
}
