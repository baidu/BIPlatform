package com.baidu.rigel.biplatform.ac.util;

/**
 * 汉字转unicode
 * 
 * @author luowenlei
 *
 */
public class UnicodeUtils {

    /**
     * unicode前缀
     */
    private static final String PREFIX = "#@#U";

    /**
     * string2Unicode
     * 
     * @param string string
     * @return string2Unicode
     */
    public static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append(PREFIX + Integer.toHexString(c));
        }
        return unicode.toString();
    }

    /**
     * unicode2String
     * 
     * @param unicode
     *            unicode
     * @return unicode2String
     */
    public static String unicode2String(String unicode) {
        StringBuffer string = new StringBuffer();
        if (unicode.indexOf(PREFIX) == 0) {
            String[] hex = unicode.split(PREFIX);
            for (int i = 1; i < hex.length; i++) {
                // 转换出每一个代码点
                int data = Integer.parseInt(hex[i], 16);
                // 追加成string
                string.append((char) data);
            }
            return string.toString();
        } else {
            return unicode;
        }

    }
}
