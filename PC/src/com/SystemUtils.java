package com;

import com.utils.*;
import java.io.*;
import java.util.*;
import mlogger.*;

public class SystemUtils {

    //////////////// sprawdzic ProcessBuilder
    private static File desktopPath;

    public static class ProcessInfo {

        public String name = "";
        public int pid = 0;
    }

    public static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static File getUserPath() {
        return new File(System.getProperty("user.home"));

    }

    public static File getDesktopPath() {

        if (desktopPath != null)
            return desktopPath;

        File usr = getUserPath();

        File desktop = new File(usr, "pulpit");
        if (!desktop.exists() || !desktop.isDirectory())
            desktop = new File(usr, "desktop");

        if (!desktop.exists() || !desktop.isDirectory())
            desktop = usr;

        desktopPath = desktop;
        return desktop;
    }

    public static List<ProcessInfo> listRunningProcesses() {
        List<ProcessInfo> processes = new ArrayList<ProcessInfo>();
        try {
            String line;
            Process p;
            boolean windows = isWindowsOS();
            if (windows)
                p = Runtime.getRuntime().exec("tasklist.exe /fo csv");
            else
                p = Runtime.getRuntime().exec("ps -eo command,pid");

            List<String> lst = new LinkedList<>();

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null)
                lst.add(line);
            input.close();

            for (int i = 1; i < lst.size(); i++)
                try {

                    ProcessInfo pi = new ProcessInfo();

                    if (windows) {
                        String[] arr = lst.get(i).split(",");
                        String s = arr[0].trim();
                        if (s.length() < 2)
                            continue;
                        pi.name = s.substring(1, s.length() - 1);

                        s = arr[1].trim();
                        if (s.length() < 2)
                            continue;
                        pi.pid = Integer.parseInt(s.substring(1, s.length() - 1));
                    } else {
                        String s = lst.get(i).trim();
                        pi.pid = Integer.parseInt(s.substring(s.lastIndexOf(" "), s.length()).trim());
                        pi.name = s.substring(0, s.lastIndexOf(" ")).trim();
                    }
                    processes.add(pi);
                } catch (Exception e) {
                }

        } catch (Exception err) {
        }
        return processes;
    }

    /*
     public static CommandResult executeCommand(String command, String encoding)
     throws IOException, InterruptedException {
     return executeCommand(command, encoding, null);
     }

     public static CommandResult executeCommand(String command, String encoding, Integer timeout)
     throws IOException, InterruptedException {
     List<String> lst = new LinkedList<>();
     lst.add(command);
     return executeCommand(lst, encoding, timeout);
     }
     */
    public static class CommandResult {

        public String buffer = "";
        public String error = "";
        public int exitValue = -1;
    }

    public static CommandResult executeCommand(String... command)
            throws IOException, InterruptedException {
        return executeCommand(null, null, command);
    }

    public static CommandResult executeCommand(Integer timeout, String encoding, String... command)
            throws IOException, InterruptedException {
        if (command == null || command.length == 0)
            throw new IOException();
        List<String> lst = new LinkedList<>();
        for (String s : command)
            lst.add(s);
        return executeCommand(timeout, encoding, lst);
    }

    public static CommandResult executeCommand(Integer timeout, String encoding, List<String> command)
            throws IOException {

        CommandResult res = new CommandResult();
        if (command == null || command.isEmpty())
            throw new IOException("No command");

        String sname = command.get(0);
        if (sname.contains(" "))
            sname = sname.substring(0, sname.indexOf(" "));
        sname = new Path(sname).getFileNameWithoutExt();

        String sCommand = new Strings().addAll(command).toString(" ");

        boolean log = true;

        Log.debug("Execute", Utils.listToString(command, " "));

        ProcessBuilder builder = new ProcessBuilder(command);

        Process exec = builder.start();

        String enc = "UTF-8";

        if (isWindowsOS())
            enc = "CP852";

        ProcessHandler ph = new ProcessHandler(exec, sname, sCommand, timeout, enc);
        ph.start();

        try {
            while (!ph.canTerminate())
                Thread.sleep(1);

            exec.waitFor();
            res.exitValue = exec.exitValue();
            ph.interrupt();
            ph.join();

        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }

        res.buffer = ph.data.toString();
        res.error = ph.error.toString();

        if (log && !res.buffer.isEmpty())
            Log.debug(sname, "Return value: " + res.exitValue, res.buffer);

        if (log && !res.error.isEmpty() && res.exitValue != 0)
            Log.warning(sname, "Return value: " + res.exitValue + "\n" + res.error, null);

        if (log && res.buffer.isEmpty()
                && res.error.isEmpty())
            Log.debug(sname, "Return value: " + res.exitValue);

        return res;
    }

    private static class ProcessHandler extends Thread {

        private final Process exec;
        public final StringWriter data = new StringWriter();
        public final StringWriter error = new StringWriter();
        private boolean canTerminate = false;
        private Integer timeout;
        private final String name;
        private final String command;
        private final String encoding;

        private ProcessHandler(Process exec, String name, String command, Integer timeout, String encoding) {
            this.exec = exec;
            this.timeout = timeout;
            this.name = name;
            this.encoding = encoding;
            this.command = command;
        }

        public boolean canTerminate() {
            return canTerminate;
        }

        @Override
        public void run() {
            InputStream in = exec.getInputStream();
            InputStream err = exec.getErrorStream();

            boolean log = true;

            long time = new Date().getTime();
            while (true)
                try {
                    canTerminate = true;
                    if (timeout != null && new Date().getTime() - time > timeout) {
                        exec.destroy();
                        Log.error(name, "Timeout");
                        return;
                    }
                    byte[] buff = new byte[10240];
                    int cnt = in.read(buff, 0, in.available());
                    if (cnt > 0) {
                        canTerminate = false;
                        String str = new String(buff, 0, cnt, encoding);
                        data.write(str);
                        if (log)
                            for (String s : str.split("\\n"))
                                Log.debug(name, s);
                    }
                    cnt = err.read(buff, 0, err.available());
                    if (cnt > 0) {
                        canTerminate = false;
                        String str = new String(buff, 0, cnt, encoding);
                        error.write(str);
                        if (log)
                            for (String s : str.split("\\n"))
                                Log.warning(name, s);
                    }
                    Thread.sleep(1);
                } catch (Exception ex) {
                    return;
                }
        }
    }

}
