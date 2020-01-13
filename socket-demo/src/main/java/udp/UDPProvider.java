package udp;

import udp.chat.MessageCreator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @description : 消息提供
 * @date : 2020-01-03 17:38
 **/
public class UDPProvider {

    public static void main(String[] args) throws IOException {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        // 读取任何键盘输入便退出
        int read = System.in.read();
        System.out.println(read);
        provider.exit();
    }

    public static class Provider extends Thread {
        private final String sn;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        public Provider(String sn) {
            this.sn = sn;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("udp.UDPProvider is Started");
            try {
                this.datagramSocket = new DatagramSocket(20000);

                while (!this.done) {
                    byte[] data = new byte[512];
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

                    // 接收消息
                    datagramSocket.receive(datagramPacket);
                    int length = datagramPacket.getLength();
                    int offset = datagramPacket.getOffset();
                    byte[] packetData = datagramPacket.getData();
                    String content = new String(packetData, offset, length, Charset.defaultCharset());
                    String ip = datagramPacket.getAddress().getHostAddress();
                    int port = datagramPacket.getPort();
                    String message = "收到消息（" + ip + ":" + port + "）\t内容：" + content;
                    System.out.println(message);

                    int responsePort = MessageCreator.parsePort(content);
                    if (responsePort != -1) {
                        String responseData = MessageCreator.buildWithSn(sn);
                        // 返回消息
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes, responseDataBytes.length,
                                datagramPacket.getAddress(), responsePort);
                        datagramSocket.send(responsePacket);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.close();
            }
            System.out.println("udp.UDPProvider is Finished");
        }

        void exit() {
            this.done = true;
            this.close();
        }

        private void close() {
            if (!Objects.isNull(this.datagramSocket)) {
                this.datagramSocket.close();
                this.datagramSocket = null;
            }
        }
    }
}
