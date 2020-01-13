package udp.chat;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.format.DateTimeFormatter;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @date : 2020-01-03 15:24
 **/
public class A {

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        System.out.println("udp.UDPProvider Started!");
//        构建一个UDP链接🔗 监听端口20000
        DatagramSocket datagramSocket = new DatagramSocket(20000);

//        构建接收实体
        final byte[] data = new byte[512];
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

        //        构建异步线程
        Sender sender = new Sender(datagramSocket, datagramPacket);
//        启动线程
        sender.start();

        B.Receiver receiver = new B.Receiver(datagramSocket, datagramPacket);
        receiver.start();

    }

    public static class Sender extends Thread {

        private DatagramSocket datagramSocket;
        private DatagramPacket datagramPacket;

        Sender(DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
            this.datagramSocket = datagramSocket;
            this.datagramPacket = datagramPacket;
        }

        @SneakyThrows
        @Override
        public void run() {
            super.run();
            boolean flag = true;
            final String exitContent = "bye";
            do {
                System.out.println("请输入内容：");
                //        构建键盘输入流
                InputStream inPut = System.in;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inPut));
                //        从键盘输入读取一行
                String reposeContent = bufferedReader.readLine();
                if (exitContent.equalsIgnoreCase(reposeContent)) {
                    flag = false;
                }
                byte[] reposeContentBytes = reposeContent.getBytes();
                //        根据发送方构建回送信息
                DatagramPacket responsePacket = new DatagramPacket(reposeContentBytes, reposeContentBytes.length);
//                responsePacket.setSocketAddress(datagramPacket.getSocketAddress());
                responsePacket.setAddress(InetAddress.getByName("192.168.1.54"));
                responsePacket.setPort(20000);
                datagramSocket.send(responsePacket);
            } while (flag);
            datagramSocket.close();
        }
    }
}
