package com.sy.cc.multicast;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Byte数组转换工具类
 */
public class ByteUtil {


    /**
     * 读取输入流中指定字节的长度
     * <p/>
     * 输入流
     *
     * @param length 指定长度
     * @return 指定长度的字节数组
     */
    public static byte[] readBytesFromTo(byte[] buffer, int from, int length) {
        byte[] sub = new byte[length];
        int cur = 0;
        for (int i = from; i < length + from; i++) {
            if (i >= buffer.length) {
                throw new ArrayIndexOutOfBoundsException("该报文长度不符合要求！");
            }
            sub[cur] = buffer[i];
            cur++;
        }
        return sub;
    }

    /**
     * 转换byte数组为int（小端）
     *
     * @return
     * @note 数组长度至少为4，按小端方式转换,即传入的bytes是小端的，按这个规律组织成int
     */
    public static int bytes2Int_LE(byte[] bytes) {
        if (bytes.length < 4) {
            return -1;
        }
        int iRst = (bytes[0] & 0xFF);
        iRst |= (bytes[1] & 0xFF) << 8;
        iRst |= (bytes[2] & 0xFF) << 16;
        iRst |= (bytes[3] & 0xFF) << 24;

        return iRst;
    }

    /**
     * 转换byte数组为Char（小端）
     *
     * @return
     * @note 数组长度至少为2，按小端方式转换
     */
    public static char bytes2Char_LE(byte[] bytes) {
        if (bytes.length < 2)
            return (char) -1;
        int iRst = (bytes[0] & 0xFF);
        iRst |= (bytes[1] & 0xFF) << 8;

        return (char) iRst;
    }

    /**
     * 转换byte数组为short（小端）
     *
     * @return
     * @note 数组长度至少为2，按小端方式转换
     */
    public static short littleByteToShort(byte[] data) {
        if (data.length != 2) {
            throw new UnsupportedOperationException("the byte length is not 2");
        }
        return ByteBuffer.allocate(data.length).put(data).getShort(0);
    }

    public static int bytes2Int(byte[] data) {
        if (data.length != 4) {
            throw new UnsupportedOperationException("the byte length is not 4");
        }
        return ByteBuffer.allocate(data.length).put(data).getInt(0);
    }

    /**
     * byte数组转换成16进制字符串
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 16进制转换为字符串
     *
     * @param hex
     * @return
     */
    public static String hexStr2Str(String hex) {
        String hexStr = "";
        String str = "0123456789ABCDEF";
        for (int i = 0; i < hex.length(); i++) {
            String s = hex.substring(i, i + 1);
            if (s.equals("a") || s.equals("b") || s.equals("c") || s.equals("d") || s.equals("e") || s.equals("f")) {
                s = s.toUpperCase().substring(0, 1);
            }
            hexStr += s;
        }

        char[] hexs = hexStr.toCharArray();
        int length = (hexStr.length() / 2);
        byte[] bytes = new byte[length];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            int position = i * 2;// 两个16进制字符 -> 1个byte数值
            n = str.indexOf(hexs[position]) * 16;
            n += str.indexOf(hexs[position + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        String name = "";
        try {
            name = new String(bytes, "GBK");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 字符串转换为Ascii
     *
     * @param value
     * @return
     */
    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    /**
     * Ascii转换为字符串
     *
     * @param value
     * @return
     */
    public static String asciiToString(String value) {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }

    public static byte[] int2ByteLe(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff); // 最高位
        targets[1] = (byte) ((res >> 8) & 0xff);
        targets[2] = (byte) (res >> 16 & 0xff);
        targets[3] = (byte) (res >> 24 & 0xff);

        return targets;
    }

}
