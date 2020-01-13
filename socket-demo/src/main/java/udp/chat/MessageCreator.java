package udp.chat;

/**
 * @author : zuoyu
 * @project : socket-demo-1
 * @description : 信息构建
 * @date : 2020-01-03 18:26
 **/
public class MessageCreator {

    public static final String SN_HEADER = "收到暗号，我是（SN）：";
    public static final String PORT_HEADER = "这是暗号，请回电端口（PORT）：";

    public static String buildWithPort(int port) {
        return PORT_HEADER + port;
    }

    public static int parsePort(String data) {
        if (data.startsWith(PORT_HEADER)){
            return Integer.parseInt(data.substring(PORT_HEADER.length()));
        }
        return -1;
    }

    public static String buildWithSn(String sn){
        return SN_HEADER + sn;
    }

    public static String parseSn(String data){
        if (data.startsWith(SN_HEADER)){
            return data.substring(SN_HEADER.length());
        }
        return null;
    }
}
