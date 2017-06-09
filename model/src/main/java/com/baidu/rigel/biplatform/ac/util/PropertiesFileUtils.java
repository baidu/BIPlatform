package com.baidu.rigel.biplatform.ac.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 类PropertiesFileUtils.java的实现描述：在System.getProperty参数中， readPropertiesFile,将
 * 文件名作为key，/home/file.properties作为value存入propertiesMap中
 * 例如：在System.getProperty参数中
 * ，如果设置为-Dproperties=/home/file1.properties,/home/file2.properties
 * 那么propertiesMap将会有2个entry，key分别为：file1，file2
 * value为/home/file1.properties及/home/file2.properties里的properties
 *
 * 
 * @author luowenlei 2015年10月29日 下午3:16:03
 */
public class PropertiesFileUtils {
    
    private static Logger logger = LoggerFactory.getLogger(PropertiesFileUtils.class);
    
    public static ConcurrentHashMap<String, Properties> propertiesMap = new ConcurrentHashMap<String, Properties>();
    
    static {
        try {
            readPropertiesFile();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 读取-Dproperties中的所有配置文件
     *
     * @throws IOException
     */
    public static void readPropertiesFile() {
        String propertiesFileName = System.getProperty("properties");
        if (!StringUtils.isEmpty(propertiesFileName)) {
            String[] propFileArray = propertiesFileName.split(",");
            for (String fileName : propFileArray) {
                Properties properties = new Properties();
                FileInputStream inStream;
                try {
                    inStream = new FileInputStream(fileName);
                    properties.load(inStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // map中的key为文件名去掉后缀
                int idx = fileName.lastIndexOf("/");
                if (idx < 0) {
                    idx = fileName.lastIndexOf("\\");
                }
                String key = fileName.substring(idx + 1,
                        fileName.lastIndexOf("."));
                propertiesMap.put(key, properties);
            }
        }
    }
    
    public static Properties getProperties(String fileName) {
        if (MapUtils.isEmpty(propertiesMap)) {
            return null;
        }
        return propertiesMap.get(fileName);
    }
    
    public static String getPropertiesKey(String fileName, String key) {
        if (MapUtils.isEmpty(propertiesMap)) {
            return null;
        }
        if (propertiesMap.get(fileName) == null) {
            return null;
        }
        if (propertiesMap.get(fileName).get(key) == null) {
            return null;
        }
        return propertiesMap.get(fileName).get(key).toString();
    }
    
    /**
     * string2Unicode
     * 
     * @param string
     *            string
     * @return string2Unicode
     */
    private static String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            if (!isChinese(c)) {
                unicode.append(c);
            } else {
                // 转换为unicode
                unicode.append("\\u" + Integer.toHexString(c));
            }
        }
        return unicode.toString();
    }
    
    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
 
    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }
}
