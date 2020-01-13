package udp.chat;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @date : 2020-01-03 15:24
 **/
public class B {
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        System.out.println("udp.udp.UDPSearcher Started!");
//        构建一个UDP链接🔗 让系统自己分配
        DatagramSocket datagramSocket = new DatagramSocket(20000);

//        构建接收实体
        final byte[] data = new byte[512];
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

        //        构建异步线程
        Sender sender = new Sender(datagramSocket);
//        启动线程
        sender.start();

        Receiver receiver = new Receiver(datagramSocket, datagramPacket);
        receiver.start();

    }


    public static class Sender extends Thread {

        private DatagramSocket datagramSocket;

        Sender(DatagramSocket datagramSocket) {
            this.datagramSocket = datagramSocket;
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
                responsePacket.setAddress(InetAddress.getByName("192.168.1.172"));
                responsePacket.setPort(20000);
                datagramSocket.send(responsePacket);
            } while (flag);
            datagramSocket.close();
        }
    }

    public static class Receiver extends Thread {
        private DatagramSocket datagramSocket;
        private DatagramPacket datagramPacket;

        Receiver(DatagramSocket datagramSocket, DatagramPacket datagramPacket) {
            this.datagramSocket = datagramSocket;
            this.datagramPacket = datagramPacket;
        }

        @SneakyThrows
        @Override
        public void run() {
            super.run();
            while (true) {
//        接收消息
                datagramSocket.receive(datagramPacket);

//        发送者的IP
                String ip = datagramPacket.getAddress().getHostAddress();
//        发送者的端口
                int port = datagramPacket.getPort();
//        发送者发送的数据
                int length = datagramPacket.getLength();
                int offset = datagramPacket.getOffset();
                byte[] packetData = datagramPacket.getData();
//        转换为字符串
                String content = new String(packetData, offset, length, Charset.defaultCharset());
                System.out.println(ip + "发送消息（" + LocalDateTime.now().format(DATETIME_FORMATTER) + "）：\t" + content);
                System.out.println("-----------------------------------------------------");
            }
        }
    }
}
