package com;

import com.FileUtils.FileParam;
import com.Path;
import com.utils.SystemProperties;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class FindFiles extends SimpleFileVisitor<java.nio.file.Path> {

    public List<FileParam> files = new LinkedList<>();
    public List<FileParam> folders = new LinkedList<>();
    private Path root;
    private boolean includeSubDirs;
    private ProgressListener progress;
    private final boolean isWindows = SystemProperties.isWindowsOS;

    public void search(String root, boolean includeSubDirs, ProgressListener progress)
            throws IOException {

        if (root == null)
            return;
        if (!root.endsWith("/"))
            root += "/";

        if (!isWindows && !root.startsWith("/"))
            root = "/" + root;

        this.progress = progress;
        this.includeSubDirs = includeSubDirs;
        this.root = new Path(root);

        Files.walkFileTree(Paths.get(root), this);
    }

    @Override
    public FileVisitResult preVisitDirectory(final java.nio.file.Path dir,
            final BasicFileAttributes attrs) {
        if (!root.isSame(dir.toString())) {
            FileParam fp = new FileParam(dir.toString());
            folders.add(fp);
            fp.fPath = dir;
            fp.isFile = false;
            fp.size = attrs.size();
            fp.lastModifiedTime = attrs.lastModifiedTime().toMillis();
            fp.lastAccessTime = attrs.lastAccessTime().toMillis();
            fp.creationTime = attrs.creationTime().toMillis();
        }

        if (progress != null && progress.isCanceled())
            return FileVisitResult.TERMINATE;

        return includeSubDirs || root.isSame(dir.toString())
                ? FileVisitResult.CONTINUE
                : FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(final java.nio.file.Path file, final BasicFileAttributes attrs) {
        FileParam fp = new FileParam(file.toString());
        files.add(fp);
        fp.fPath = file;
        fp.isFile = true;
        fp.size = attrs.size();
        fp.lastModifiedTime = attrs.lastModifiedTime().toMillis();
        fp.lastAccessTime = attrs.lastAccessTime().toMillis();
        fp.creationTime = attrs.creationTime().toMillis();

        if (progress != null) {
            progress.incrTotalMax(fp.size);
            progress.setItemName(fp.path.getFileName());
        }
        if (progress != null && progress.isCanceled())
            return FileVisitResult.TERMINATE;

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final java.nio.file.Path file, IOException exc) throws IOException {
        if (progress != null)
            progress.addError(exc);
        if (!root.isSame(file.toString())) {
            folders.add(new FileParam(file.toString()));
            return FileVisitResult.CONTINUE;
        }
        throw exc;
    }
}
