package tcp;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @description : TCP服务器
 * @date : 2020-01-08 10:55
 **/
public class TCPServer {

    private static final int PORT = 20000;

    @SneakyThrows
    public static void main(String[] args) {
        ServerSocket serverSocket = createServerSocket();
        initServerSocket(serverSocket);
//        绑定到本地端口
        serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        System.out.println("服务器准备就绪～");
        System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());

        // 等待客户端连接
        while (true) {
            // 得到客户端
            Socket client = serverSocket.accept();
            // 客户端构建异步线程
            ClientHandler clientHandler = new ClientHandler(client);
            // 启动线程
            clientHandler.start();
        }
    }

    @SneakyThrows
    private static ServerSocket createServerSocket() {
        // 创建基础的ServerSocket
        ServerSocket serverSocket = new ServerSocket();

        // 绑定到本地端口20000上，并且设置当前可允许等待链接的队列为50个
        //serverSocket = new ServerSocket(PORT);

        // 等效于上面的方案，队列设置为50个
        //serverSocket = new ServerSocket(PORT, 50);

        // 与上面等同
        // serverSocket = new ServerSocket(PORT, 50, Inet4Address.getLocalHost());

        return serverSocket;
    }

    @SneakyThrows
    private static void initServerSocket(ServerSocket serverSocket) {
        // 是否复用未完全关闭的地址端口
        serverSocket.setReuseAddress(true);

        // 等效Socket#setReceiveBufferSize
        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置serverSocket#accept超时时间
        // serverSocket.setSoTimeout(2000);

        // 设置性能参数：短链接，延迟，带宽的相对重要性
        serverSocket.setPerformancePreferences(1, 1, 1);
    }

    /**
     * 客户端消息处理
     */
    private static class ClientHandler extends Thread {
        private Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @SneakyThrows
        @Override
        public void run() {
            super.run();
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[256];
            int read = inputStream.read(bytes);
            if (read > 0) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, read);

                byte b = byteBuffer.get();
                char c = byteBuffer.getChar();
                short s = byteBuffer.getShort();
                int i = byteBuffer.getInt();
                boolean bool = byteBuffer.get() == 1;
                long l = byteBuffer.getLong();
                float f = byteBuffer.getFloat();
                double d = byteBuffer.getDouble();
                int position = byteBuffer.position();
                String string = new String(bytes, position, read - position - 1);

                System.out.println("服务器得到数据：" + read + "个。\t内容：\nb:"
                        + b + "\nc:"
                        + c + "\ns:"
                        + s + "\ni:"
                        + i + "\nbool:"
                        + bool + "\nl:"
                        + l + "\nf:"
                        + f + "\nd:"
                        + d + "\nstring:"
                        + string + "\n");
                outputStream.write(bytes, 0, read);
            } else {
                System.out.println("服务器无数据：\t" + read);
                outputStream.write(new byte[]{0});
            }
            outputStream.close();
            inputStream.close();
        }
    }

}
