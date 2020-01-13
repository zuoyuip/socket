package basics;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @description : 服务器端
 * @date : 2020-01-02 10:34
 **/
public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(2000);
        System.out.println("服务器准备就绪......");
        System.out.println("服务器信息：" + server.getInetAddress() + "P：" + server.getLocalPort());

        while (true) {
//            得到客户端
            Socket client = server.accept();
//            构建异步线程
            ClientHandler clientHandler = new ClientHandler(client);
//            启动线程
            clientHandler.start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket client;
        private boolean flag;

        ClientHandler(Socket client) {
            this.client = client;
        }

        @SneakyThrows
        @Override
        public void run() {
            super.run();
            System.out.println("新客户端连接：" + client.getInetAddress() + "P：" + client.getPort());

            try {
//                得到打印流，用于数据的输出
                PrintStream socketOutput = new PrintStream(client.getOutputStream());
//                得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(client.getInputStream()));

                final String exitContent = "bye";
                boolean flag = true;

                do {
                    String inContent = socketInput.readLine();
                    if (exitContent.equalsIgnoreCase(inContent.trim())) {
                        flag = false;
                        System.out.println("客户端关闭");
                    }
                    System.out.println("服务器接收：\t" + inContent);
                    socketOutput.println("回送：\t" + inContent.length());
                } while (flag);
                socketOutput.close();
                socketInput.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("连接异常！");
            } finally {
                this.client.close();
            }
        }
    }
}
