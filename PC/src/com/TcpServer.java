package com;

//import com.servlet.interfaces.events.OnContextDestroyed;
import com.TcpServer.TcpClientSocket;
import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import mlogger.Log;


public class TcpServer<T extends TcpClientSocket> implements Closeable {

    private final static LinkedList<TcpServer> servers = new LinkedList<TcpServer>();

    //   @OnContextDestroyed
    public static void onContextDestroyed() {
        for (TcpServer s : getServers())
            try {
                s.close();
            } catch (IOException ex) {
            }
    }

    public static LinkedList<TcpServer> getServers() {
        LinkedList<TcpServer> list = new LinkedList<TcpServer>();
        synchronized (servers) {
            list.addAll(servers);
        }
        return list;
    }

    public static abstract class TcpClientSocket extends Thread implements Runnable, Closeable {

        protected final Socket socket;
        protected final TcpServer server;

        public TcpClientSocket(TcpServer server, Socket socket) throws Exception {
            this.socket = socket;
            this.server = server;
        }

        public abstract void execute(Socket socket) throws Throwable;

        @Override
        public void close() throws IOException {
            socket.close();
        }

        @Override
        public void run() {

            try {
                try {
                    execute(socket);
                } catch (Throwable ex) {
                    server.onException(ex, this);
                }

            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    server.onException(ex, this);
                }
                synchronized (server.clients) {
                    server.clients.remove(this);
                }
            }

        }

    }

    //VT100
    final ServerSocket server;
    private Thread thread;
    private final LinkedList<T> clients = new LinkedList<T>();
    private final String name;
    private final Class<T> socketClass;

    public TcpServer(String name, Class<T> socketClass) throws IOException {
        this.server = new ServerSocket();
        this.name = name;
        this.socketClass = socketClass;
    }

    protected void onException(Throwable e, TcpClientSocket socket) {
        Log.error(e);
    }

    public LinkedList<T> getClients() {
        LinkedList<T> list = new LinkedList<T>();
        synchronized (clients) {
            list.addAll(clients);
        }
        return list;
    }

    public void start(final SocketAddress address) throws IOException {

        server.bind(address);

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    while (!thread.isInterrupted()) {
                        T client = null;
                        Socket socket = null;
                        try {

                            socket = server.accept();

                            client = socketClass.getConstructor(TcpServer.class, Socket.class)
                                    .newInstance(TcpServer.this, socket);

                            synchronized (clients) {
                                clients.add(client);
                            }

                            client.setName(name + " " + address.toString()
                                    + " <- " + socket.getInetAddress());
                            client.start();

                        } catch (Throwable ex) {
                            if (socket != null)
                                try {
                                    socket.close();
                                } catch (IOException ex1) {
                                    onException(ex, client);
                                }
                            onException(ex, client);
                        }

                    }
                } finally {
                    try {
                        server.close();
                    } catch (IOException ex) {
                        onException(ex, null);
                    }
                }

            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setName(name + " " + address.toString());
        thread.start();

        synchronized (servers) {
            servers.add(this);
        }

    }

    @Override
    public void close() throws IOException {
        for (T client : getClients())
            try {
                client.socket.close();
            } catch (Throwable e) {
                onException(e, client);
            }

        try {
            server.close();
        } catch (Throwable ex) {
            onException(ex, null);
        }
        if (thread != null)
            thread.interrupt();

        synchronized (servers) {
            servers.remove(this);
        }
    }
}
