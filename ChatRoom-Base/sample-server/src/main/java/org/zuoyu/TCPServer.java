package org.zuoyu;

import org.zuoyu.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zuoyu
 */
public class TCPServer implements ClientHandler.ClientHandlerCallBack {
    private final int port;
    private final ExecutorService forwardingThreadPoolExecutor;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
//        转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }

        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }

            clientHandlerList.clear();
        }
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(final ClientHandler clientHandler, String message) {
        System.out.println("Received-" + clientHandler.getClientInfo() + ":\t" + message);
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler handler : clientHandlerList) {
                    if (clientHandler.equals(handler)) {
                        continue;
                    }
                    handler.send(message);
                }
            }
        });
    }

    private class ClientListener extends Thread {
        private ServerSocket server;
        private boolean done = false;

        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        @Override
        public void run() {
            super.run();

            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                try {
                    // 客户端构建异步线程
                    ClientHandler clientHandler = new ClientHandler(client, TCPServer.this);
                    // 读取数据并打印
                    clientHandler.readToPrint();
//                    添加同步
                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            } while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
