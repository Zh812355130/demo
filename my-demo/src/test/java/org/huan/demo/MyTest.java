package org.huan.demo;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;

import static java.lang.Math.*;

public class MyTest {
    private static final int R = 6371; // 地球半径，单位为米
    public static void main(String[] args) {

        double lat1 = 31.85766; // 北京
        double lon1 = 117.27652;
        double lat2 = 31.8388; // 洛杉矶
        double lon2 = 117.27878;
        double distance1 = calculateDistance(lat1, lon1, lat2, lon2);
        System.out.println("1两点之间的距离: " + distance1 + " 千米");
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) *
                        sin(dLon / 2) * sin(dLon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return R * c; // 返回距离，单位为米
    }


    private static void testCheckSum() {
        //        int cmdSeq = 65535;
//        byte byteL = (byte) (cmdSeq & 0XFF);
//        byte byteH = (byte) ((cmdSeq >> 8) & 0XFF);
//        byte[] seqBytes = ByteUtil.intToBytes(cmdSeq);

        String str = StrUtil.replace("55 58 00  08 00 ff ff 01 00 00 00 09 00 00 00"," ","") ;
        System.out.println(str);
        byte[] data  = HexUtil.decodeHex(str);
//        byte[] data = new byte[]{85, 25, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0};
        // 示例数据，已知的字节数组
//        byte[] data = { (byte) 0xF1, (byte) 0x32, (byte) 0xFC , (byte) 0xFC , (byte) 0xFC , (byte) 0xFC , (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32}; // 已知字节数组

        // 计算校验数
        byte checksum = calculateChecksum(data);
        System.out.printf("Calculated Checksum: 0x%02X%n", checksum);

        // 将校验数加入到字节数组中
        byte[] fullData = new byte[data.length + 1];
        System.arraycopy(data, 0, fullData, 0, data.length);
        fullData[data.length] = checksum;

        // 验证整个数组的校验和
        int finalSum = 0; // 用于验证总和
        for (byte b : fullData) {
            finalSum += (b & 0xFF); // 无符号相加
        }
        System.out.println((finalSum & 0XFF) == 0XFF);
        System.out.printf("Final Sum after adding: 0x%02X%n", finalSum % 256);
    }

    // 计算给定字节数组的校验数使得整体的校验和为 0xFF
    public static byte calculateChecksum(byte[] data) {
        int sum = 0; // 初始化累加和

        // 遍历每个字节，累加其值
        for (byte b : data) {
            sum += (b & 0xFF); // 转换为无符号整数相加
//            sum += b; // 转换为无符号整数相加
        }

        // 计算校验数，使得整体和为 0xFF
//        int checksum = (0xFF - (sum % 256)) % 256; // 得到需要的校验数
        int checksum = 0xFF - (sum &0xff); // 得到需要的校验数
        System.out.println(checksum);
        return (byte) checksum;
    }


}
