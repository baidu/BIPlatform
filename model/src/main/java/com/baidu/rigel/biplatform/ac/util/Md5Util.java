/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.ac.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.StringUtils;

/**
 * md5加密工具类
 * 
 * @author xiaoming.chen
 *
 */
public class Md5Util {
    
    /**
     * MD5普通加密
     * 
     * @param rawPass 明文
     * @return 字符串的md5
     */
    public static String encode(String rawPass) {
        return encode(rawPass, null);
    }
    
    /**
     * * MD5盐值加密
     * 
     * @param rawPass 明文
     * @param salt 盐值
     * @return 字符串的md5
     * @throws IllegalArgumentException 当第一个参数明文为空出现此异常
     */
    public static String encode(String rawPass, Object salt) {
        if (StringUtils.isBlank(rawPass)) {
            throw new IllegalArgumentException("encode string can not be empty!");
        }
        String saltedPass = mergePasswordAndSalt(rawPass, salt);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(saltedPass.getBytes());
            return new String(decodeByteArray(digest));
        } catch (NoSuchAlgorithmException e) {
            return rawPass;
        }
    }
    
    /**
     * 
     * 拼接密码与盐值
     * 
     * @param password 字符串
     * @param salt 盐值
     * @return 密码{盐值}
     */
    private static String mergePasswordAndSalt(String password, Object salt) {
        if (salt == null || "".equals(salt.toString().trim())) {
            return password;
        }
        return password + "{" + salt.toString() + "}";
    }
    
    /**
     * encode
     * 
     * @param bytes 二进制数组
     * @return 转换后的char数组
     */
    private static char[] decodeByteArray(byte[] bytes) {
        char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
                'f' };
        int nBytes = bytes.length;
        char[] result = new char[2 * nBytes];
        int j = 0;
        for (int i = 0; i < nBytes; i++) {
            // Char for top 4 bits
            result[j++] = hex[(0xF0 & bytes[i]) >>> 4];
            
            // Bottom 4
            result[j++] = hex[(0x0F & bytes[i])];
        }
        return result;
    }
    
}