package api;

import api.TCP.TcpSocket;
import com.TcpServer;
import com.TcpServer.TcpClientSocket;
import java.io.IOException;
import java.net.Socket;

public class TCP extends TcpServer<TcpSocket> {

    public TCP(String name, Class<TcpSocket> socketClass) throws IOException {
        super(name, socketClass);
    }

    public static class TcpSocket extends TcpClientSocket {

        public TcpSocket(TcpServer server, Socket socket) throws Exception {
            super(server, socket);
        }

        @Override
        public void execute(Socket socket) throws Throwable {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
