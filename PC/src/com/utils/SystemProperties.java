package com.utils;

public interface SystemProperties {

    public final static String encoding = System.getProperty("file.encoding");
    public final static String path = System.getProperty("java.class.path");
    public final static String fileSeparator = System.getProperty("file.separator");
    public final static String javaExtDirs = System.getProperty("java.ext.dirs");
    public final static String javaHome = System.getProperty("java.home");
    public final static String temp = System.getProperty("java.io.tmpdir");
    public final static String jreName = System.getProperty("java.runtime.name");
    public final static String jreVersion = System.getProperty("java.runtime.version");
    // public final String javaVersion = System.getProperty("java.version");
    public final static String javaVersion = System.getProperty("java.specification.version");
    public final static String javaVmName = System.getProperty("java.vm.name");
    public final static String javaVmInfo = System.getProperty("java.vm.info");
    public final static String javaVmVersion = System.getProperty("java.vm.version");
    //public final String javaSpecName = System.getProperty("java.vm.specification.name");
    public final static String osArch = System.getProperty("os.arch");
    public final static String osName = System.getProperty("os.name");
    public final static String javaArch = System.getProperty("sun.arch.data.model");
    public final static String cpu = System.getProperty("sun.cpu.isalist");
    public final static String desktop = System.getProperty("sun.desktop");
    public final static String language = System.getProperty("user.language");
    public final static String country = System.getProperty("user.country");
    public final static String userName = System.getProperty("user.name");
    public final static String userHome = System.getProperty("user.home");
    public final static String timeZone = System.getProperty("user.timezone");
    //  public final String javaCompiler = System.getProperty("java.compiler");
    public final static String catalinaHome = System.getProperty("catalina.home");
    public final static boolean isWindowsOS = osName != null && osName.toLowerCase().contains("windows");
    public final static boolean isLinuxOS = osName != null
            && (osName.toLowerCase().contains("linux")
            || osName.toLowerCase().contains("unix"));
    public final static boolean isJava64bit = javaArch != null && javaArch.equals("64");
    public final static boolean isCpu64bit = cpu != null && cpu.contains("64");
}
