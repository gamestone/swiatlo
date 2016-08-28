package com;

import com.io.IOUtils;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**

 @author Miłosz Ziernik
 */
public final class FileUtils {

    public static class FileParam implements Comparable<FileParam> {

        public boolean isFile;
        public Path path;
        public long size;
        public long lastModifiedTime;
        public long lastAccessTime;
        public long creationTime;
        public boolean isUnknown = false;
        public boolean isLink;
        public java.nio.file.Path fPath;

        @Override
        public String toString() {
            return getName();
        }

        public FileParam(String fPath) {
            path = new Path(fPath);
        }

        public FileParam(String fPath, String fName) {
            path = new Path(fPath, fName);
        }

        public FileParam(Path fPath, String fName) {
            path = new Path(fPath, fName);
        }

        public String getName() {
            return path.getFileName();
        }

        @Override
        public int compareTo(FileParam o) {
            return Utils.collator.compare(path.toString().toLowerCase(),
                    o.path.toString().toLowerCase());
        }
    }

    public static BufferedReader getFileReaderUtf(String file) throws FileNotFoundException {
        return getFileReader(new File(file), "UTF-8");
    }

    public static BufferedReader getFileReaderUtf(String file, String encoding) throws FileNotFoundException {
        return getFileReader(new File(file), encoding);
    }

    public static BufferedReader getFileReaderUtf(File file) throws FileNotFoundException {
        return getFileReader(file, "UTF-8");
    }

    public static BufferedReader getFileReader(File file, String encoding) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(
                new FileInputStream(file)), Charset.forName(encoding)));
    }

    public static BufferedWriter getFileWriterUtf(String file) throws FileNotFoundException {
        return getFileWriter(new File(file), "UTF-8");
    }

    public static BufferedWriter getFileWriterUtf(String file, String encoding) throws FileNotFoundException {
        return getFileWriter(new File(file), encoding);
    }

    public static BufferedWriter getFileWriterUtf(File file) throws FileNotFoundException {
        return getFileWriter(file, "UTF-8");
    }

    public static BufferedWriter getFileWriter(File file, String encoding) throws FileNotFoundException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new BufferedOutputStream(
                                new FileOutputStream(file)),
                        Charset.forName(encoding)));
    }

    public static String[] readLines(String filename) throws IOException {
        return readLines(new FileReader(filename));
    }

    public static String[] readLines(InputStream in) throws IOException {
        return readLines(new InputStreamReader(in));
    }

    public static String[] readLines(Reader in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(in);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null)
            lines.add(line);
        bufferedReader.close();
        return lines.toArray(new String[lines.size()]);
    }

    public static String getTempPath() {
        String tmp = System.getProperty("java.io.tmpdir");
        if (!tmp.endsWith(File.separator))
            tmp += File.separator;
        return tmp;
    }

    public static String IncludePathSeparator(String dir) {
        String s = dir;
        if (s != null) {
            if (!s.endsWith(File.separator))
                s += File.separator;;
        }
        return s;
    }

    public static void copyFileOrDir(File source,
            File targetDir, boolean override)
            throws IOException {

        if (!targetDir.exists())
            targetDir.mkdirs();

        if (source.isDirectory()) {

            String[] children = source.list();
            for (int i = 0; i < children.length; i++)
                copyFileOrDir(new File(source, children[i]),
                        new File(targetDir, children[i]), override);
        } else {

            File destination = new File(targetDir, source.getName());

            if (destination.exists()) {
                if (override)
                    destination.delete();
                if (destination.exists())
                    throw new IOException("\"" + destination.getAbsolutePath() + "\" już istnieje!\n");
            }
            IOUtils.copy(source, destination);
        }
    }

    /**
     Przycina zbyt dlugie nazwy plików dodając "… " przed rozszerzeniem
     (jesli balance == null) lub w srodku jako "[… ]" jesli parametr balnace jest zdefiniowany)
     */
    public static String trimFileName(String fileName, int maxLength) {
        return trimFileName(fileName, maxLength, 0.6d);
    }

    public static String trimFileName(String fileName, int maxLength, Double balance) {
        if (fileName == null)
            return fileName;

        if (balance != null && balance < 0)
            balance = 0d;
        if (balance != null && balance > 1)
            balance = 1d;

        fileName = fileName.trim();
        if (fileName.length() <= maxLength || maxLength < 4)
            return fileName;
        if (balance == null && fileName.indexOf(".") < 0)
            return fileName.substring(0, maxLength - 1).trim() + "… ";

        String ext = fileName.contains(".")
                     ? fileName.substring(fileName.lastIndexOf("."), fileName.length())
                     : "";
        if (balance == null) {
            fileName = fileName.substring(0, maxLength - ext.length() - 1).trim();
            return fileName.trim() + "… " + ext;
        }
        String left = fileName.substring(0, (int) (fileName.length() * balance)).trim();
        String right = fileName.substring(left.length(), fileName.length()).trim();
        int diff = 1 + (int) (Math.ceil((double) (left.length() + right.length()
                + ext.length() + 3) - maxLength) / 2d);
        if (diff < 0)
            diff = 0;

        left = left.substring(0, left.length() - diff);
        right = right.substring(diff, right.length());
        fileName = left + "[…]" + right;

        return fileName.trim() + ext;
    }

    public static String formatFileName(String fileName) {

        if (fileName == null)
            return null;

        final String src = "ąćęśźńżółĄĆĘŚŹŃŻÓŁ";
        final String dest = "acesznzolACESZNZOL";
        final String reserved = "/\\?*:|\"<>|";

        String result = "";
        char c;
        boolean found;

        for (int i = 0; i < fileName.length(); i++) {
            c = fileName.charAt(i);
            found = false;

            for (int j = 0; j < src.length(); j++)
                if (c == src.charAt(j)) {
                    result += dest.charAt(j);
                    found = true;
                    break;
                }

            if (!found)
                for (int j = 0; j < reserved.length(); j++)
                    if (c == reserved.charAt(j)) {
                        result += ' ';
                        found = true;
                        break;
                    }

            if (!found)
                if (c > 127)
                    result += "_";
                else
                    result += c;
        }

        return result;
    }

    public static void UnzipAll(String inputZip, String destinationDirectory)
            throws IOException {
        int BUFFER = 2048;
        List<String> zipFiles = new LinkedList<>();
        File sourceZipFile = new File(inputZip);
        File unzipDestinationDirectory = new File(destinationDirectory);
        unzipDestinationDirectory.mkdir();

        ZipFile zipFile;
        zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
        Enumeration zipFileEntries = zipFile.entries();

        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();

            File destFile = new File(unzipDestinationDirectory, currentEntry);
            destFile = new File(unzipDestinationDirectory, destFile.getName());

            if (currentEntry.endsWith(".zip"))
                zipFiles.add(destFile.getAbsolutePath());

            File destinationParent = destFile.getParentFile();
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is
                        = new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest
                        = new BufferedOutputStream(fos, BUFFER);

                while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                    dest.write(data, 0, currentByte);
                dest.flush();
                dest.close();
                is.close();
                fos.close();
            }
        }
        zipFile.close();

        for (Iterator iter = zipFiles.iterator(); iter.hasNext();) {
            String zipName = (String) iter.next();
            UnzipAll(
                    zipName,
                    destinationDirectory
                    + File.separatorChar
                    + zipName.substring(0, zipName.lastIndexOf(".zip")));
        }
    }

    public static List<FileParam> createFileList(Collection<String> list, Boolean includeSubDirs,
            ProgressListener progress)
            throws Exception {

        if (progress != null) {
            progress.setCurrent(-1);
            progress.setCaption("Tworzenie listy plików");
        }

        List<FileParam> files = new LinkedList<>();

        for (String sFile : list) {
            if (progress != null && progress.isCanceled())
                break;

            File ff = new File(sFile);
            if (!ff.exists()) {
                if (progress != null)
                    progress.addError(
                            new Exception("Nie znaleziono \""
                                    + ff.getAbsolutePath() + "\"!"));
                continue;
            }

            if (ff.isDirectory()) {
                FindFiles find = new FindFiles();
                find.search(ff.getPath(), includeSubDirs, progress);
                for (FileParam fp : find.files)
                    files.add(fp);
            } else
                if (ff.isFile()) {
                    FileParam fp = new FileParam(ff.getPath());
                    files.add(fp);
                    fp.isFile = true;
                    fp.size = ff.length();
                    fp.lastModifiedTime = ff.lastModified();
                    if (progress != null)
                        progress.incrTotalMax(fp.size);
                }
        }
        return files;
    }
    /*
     private static void ProcessFileList(File[] in, List<File> out,
     ProgressListener progress) throws Exception {

     if (progress != null && progress.isCanceled()) {
     return;
     }

     for (int i = 0; i < in.length; i++) {
     File ff = in[i];
     if (!ff.exists()) {
     if (progress != null) {
     progress.addError(
     new Exception("Nie znaleziono \""
     + ff.getAbsolutePath() + "\"!"));
     }
     continue;
     }

     if (ff.isDirectory()) {
     File[] flst = ff.listFiles();
     if (flst == null) {
     if (progress != null) {
     progress.addError(
     new Exception("Brak dostępu do katalogu \""
     + ff.getAbsolutePath() + "\"!"));
     }
     }
     ProcessFileList(flst, out, progress);
     } else if (ff.isFile()) {
     progress.incrTotalMax(ff.length());
     out.add(ff);
     }
     }
     }
     */
    //--------------------------------------------------------------------------

    public static void sortFilesC(ArrayList<File> list, final int type,
            final boolean invert) {

        //type: 1 - nazwa, 2 - rozmiar, 3 - data
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                int result = 0;

                if (type == 1)
                    result = f1.compareTo(f2);

                if (type == 2) {
                    if (f1.length() > f2.length())
                        result = -1;
                    if (f1.length() < f2.length())
                        result = 1;
                }

                if (type == 3) {
                    if (f1.lastModified() > f2.lastModified())
                        result = -1;
                    if (f1.lastModified() < f2.lastModified())
                        result = 1;
                }

                if (invert)
                    result = result * -1;

                return result;

            }
        });

    }

    public static File[] sortFiles(File[] list, int type, boolean invert) {

        File[] result;

        ArrayList<File> aFiles = new ArrayList<>();
        ArrayList<File> aFolders = new ArrayList<>();

        for (int i = 0; i < list.length; i++)
            if (list[i].isFile())
                aFiles.add(list[i]);
            else
                aFolders.add(list[i]);

        // jesli sortowanie po rozmiarze to posortuj foldery po nazwie
        if (type == 2)
            sortFilesC(aFolders, 1, invert);
        else
            sortFilesC(aFolders, type, invert);

        sortFilesC(aFiles, type, invert);

        result = new File[aFiles.size() + aFolders.size()];

        for (int i = 0; i < aFolders.size(); i++)
            result[i] = aFolders.get(i);

        for (int i = 0; i < aFiles.size(); i++)
            result[i + aFolders.size()] = aFiles.get(i);

        return result;

    }

    public static void saveStringISO88592(String str, String file) throws IOException {
        save(str.getBytes("ISO-8859-2"), new File(file));
    }

    public static void saveStringISO88592(String str, File file) throws IOException {
        save(str.getBytes("ISO-8859-2"), file);
    }

    public static void saveStringUtf8(String str, String file) throws IOException {
        save(str.getBytes("UTF-8"), new File(file));
    }

    public static void saveStringUtf8(String str, File file) throws IOException {
        save(str.getBytes("UTF-8"), file);
    }

    public static void save(byte[] content, String file) throws IOException {
        save(content, new File(file));
    }

    public static void save(byte[] content, File file) throws IOException {
        if (file == null)
            throw new FileNotFoundException();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs();

        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(content);
        } finally {
            out.close();
        }
    }

    public static byte[] load(File file) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        try {
            byte[] buff = new byte[fis.available()];
            fis.read(buff);
            return buff;
        } finally {
            fis.close();
        }
    }

    /**
     Usuń katalog wraz z podkatalogami
     */
    public static boolean deleteDir(File dir, boolean excludeRoot) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null)
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]), false);
                    if (!success)
                        return false;
                }
        }

        if (excludeRoot)
            return true;
        return dir.delete();
    }

    public static File getUniqueFileName(File file) {
        if (file == null)
            return null;

        File f = file;
        int idx = 2;
        while (f.exists()) {
            Path path = new Path(file);
            f = new Path(f.getParent(), path.getFileNameWithoutExt()
                    + " (" + idx++ + ")." + path.getFileExt()).getFile();
        }

        return f;
    }

    public static class SearchItem {

        public final boolean isFile;
        public final java.nio.file.Path path;
        public final BasicFileAttributes attrs;

        public SearchItem(boolean isFile, java.nio.file.Path path,
                BasicFileAttributes attrs) {
            this.isFile = isFile;
            this.path = path;
            this.attrs = attrs;
        }
    }

    public static class SearchFiles extends SimpleFileVisitor<java.nio.file.Path> {

        public final Path root;
        private final boolean includeSubDirs;
        public final List<SearchItem> items = new LinkedList<>();
        public final List<SearchItem> files = new LinkedList<>();
        public final List<SearchItem> folders = new LinkedList<>();

        public SearchFiles(String root, boolean includeSubDirs)
                throws IOException {
            this.root = new Path(root);
            this.includeSubDirs = includeSubDirs;

            if (root == null)
                return;
            if (!root.endsWith("/"))
                root += "/";

            if (!SystemUtils.isWindowsOS() && !root.startsWith("/"))
                root = "/" + root;

            Files.walkFileTree(Paths.get(root), this);
        }

        @Override
        public FileVisitResult preVisitDirectory(final java.nio.file.Path dir,
                final BasicFileAttributes attrs) {
            if (!root.isSame(dir.toString())) {
                SearchItem si = new SearchItem(true, dir, attrs);
                items.add(si);
                folders.add(si);
            }
            return includeSubDirs || root.isSame(dir.toString())
                   ? FileVisitResult.CONTINUE
                   : FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFile(final java.nio.file.Path file, final BasicFileAttributes attrs) {
            SearchItem si = new SearchItem(false, file, attrs);
            items.add(si);
            files.add(si);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final java.nio.file.Path file,
                IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
