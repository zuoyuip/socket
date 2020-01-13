package basics;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @description : 客户端
 * @date : 2020-01-02 10:34
 **/
public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
//        连接本地，端口2000， 超时3秒
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 3000);
        System.out.println("已发起服务器连接......");
        System.out.println("客户端信息：" + socket.getLocalAddress() + "P：" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + "P：" + socket.getPort());

        try {
            todo(socket);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("异常关闭：" + e.getLocalizedMessage());
        } finally {
            socket.close();
            System.out.println("客户端关闭");
        }
    }

    /**
     * 发送数据
     */
    private static void todo(Socket client) throws IOException {
//        构建键盘输入流
        InputStream inPut = System.in;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inPut));

//        得到socket的输出流，并转换为打印流
        OutputStream out = client.getOutputStream();
        PrintStream outPrintStream = new PrintStream(out);

//        得到socket的输入流，并转换为BufferedReader
        InputStream in = client.getInputStream();
        BufferedReader socketReader = new BufferedReader(new InputStreamReader(in));

        final String exitContent = "bye";
        boolean flag = true;

        do {
//        从键盘输入读取一行
            String inContent = bufferedReader.readLine();
//        发送出去（发送给服务器）
            outPrintStream.println(inContent);

//        从服务器返回中读取一行
            String data = socketReader.readLine();
            if (exitContent.equalsIgnoreCase(data.trim())){
                flag = false;
            }
            System.out.println(data);
        } while (flag);

        inPut.close();
        bufferedReader.close();
        out.close();
        outPrintStream.close();
        in.close();
        socketReader.close();
    }
}
