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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

/**
 * 用来进行AES的加密和解密程序
 * 
 * @author xiaoming.chen
 * 
 */
public class AesUtil {

    public static final String UTF_8 = "utf-8";

    /**
     * KEY_ALGORITHM 加密算法
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * DEFAULT_AES_KEY_VALUE 默认AES加密的密钥为16个0
     */
    public static final String DEFAULT_AES_KEY_VALUE = "0000000000000000";

    /**
     * construct with
     */
    private AesUtil() {
    }

    /**
     * aesEncrp 加密单例
     */
    private static final AesUtil INSTANCE = new AesUtil();

    /**
     * 取得AES加密的实现
     * 
     * @return 返回AESencrp
     */
    public static AesUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 用来进行加密的操作，加密过程抛出异常，直接抛出异常
     * 
     * @param data 明文
     * @param keyValue 密钥
     * @return 加密后字符串
     * @throws IllegalArgumentException 明文为空或者密钥为空
     * @throws Exception InvalidKeyException或者IllegalBlockSizeException, BadPaddingException，一般不会出现
     */
    public String encrypt(String data, String keyValue) throws Exception {
        if (StringUtils.isBlank(data)) {
            throw new IllegalArgumentException("encode string can not be blank!");
        }
        Key key = generateKey(keyValue);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);;
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = cipher.doFinal(data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    /**
     * 使用默认密钥加密
     * 
     * @param data 明文
     * @return 加密后字符串
     * @throws IllegalArgumentException 明文为空
     * @throws Exception 加密中出现的异常
     */
    public String encrypt(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            throw new IllegalArgumentException("encode string can not be blank!");
        }
        return encrypt(data, DEFAULT_AES_KEY_VALUE);
    }

    /**
     * 先用AES加密，再用URLEncode编码
     * 
     * @param data 明文
     * @param keyValue 密钥
     * @return 加密后字符串
     * @throws IllegalArgumentException 明文为空
     * @throws Exception 加密中出现的异常
     */
    public String encryptAndUrlEncoding(String data, String keyValue) throws Exception {
        String encryptValue = encrypt(data, keyValue);
        return URLEncoder.encode(encryptValue, UTF_8);
    }

    /**
     * 先用AES加密，再用URLEncode编码
     * 
     * @param data 明文
     * @return 加密后字符串
     * @throws IllegalArgumentException 明文为空
     * @throws Exception 加密中出现的异常
     */
    public String encryptAndUrlEncoding(String data) throws Exception {
        String encryptValue = encrypt(data);
        return URLEncoder.encode(encryptValue, UTF_8);
    }

    /**
     * 用来进行解密的操作，如果解密过程出现异常，直接抛出
     * 
     * @param encryptedData 待解密字符串
     * @param keyValue 密钥
     * @return 解密后的字符串
     * @throws IllegalArgumentException 密文为空或者密钥为空
     * @throws Exception 解密出现异常
     */
    public String decrypt(String encryptedData, String keyValue) throws Exception {
        if (StringUtils.isBlank(encryptedData)) {
            throw new IllegalArgumentException("decode string can not be blank!");
        }
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);;
        Key key = generateKey(keyValue);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = cipher.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    /**
     * 使用默认密钥解密
     * 
     * @param encryptedData 待解密的密文
     * @return 解密后的字符串
     * @throws IllegalArgumentException 密文为空
     * @throws Exception 解密出现异常
     */
    public String decrypt(String encryptedData) throws Exception {
        if (StringUtils.isBlank(encryptedData)) {
            throw new IllegalArgumentException("decode string can not be blank!");
        }
        return decrypt(encryptedData, DEFAULT_AES_KEY_VALUE);
    }

    /**
     * 先用urlDecode然后再解密
     * 
     * @param encryptedData 待解密字符串
     * @param keyValue 密钥
     * @return 解密后的字符串
     * @throws Exception 解密出现异常
     */
    public String decodeAnddecrypt(String encryptedData, String keyValue) throws Exception {
        String decodeString = URLDecoder.decode(encryptedData, UTF_8);
        return decrypt(decodeString, keyValue);
    }

    /**
     * 先用urlDecode然后再解密
     * 
     * @param encryptedData 加密的数据
     * @return 解密后的字符串
     * @throws Exception 解密出现的一次
     */
    public String decodeAnddecrypt(String encryptedData) throws Exception {
        String decodeString = URLDecoder.decode(encryptedData, UTF_8);
        return decrypt(decodeString);
    }

    /**
     * 根据密钥和算法生成Key
     * 
     * @param keyValue 生成加密的密钥
     * @return 返回加密的KEY
     */
    private Key generateKey(String keyValue) {
        Key key = new SecretKeySpec(checkKeyValue(keyValue).getBytes(), KEY_ALGORITHM);
        return key;
    }

    /**
     * 校验密钥，如果密钥为空，抛异常 <li>密钥大于16位，取前16位</li> <li>密钥小于16位，在后面补0</li>
     * 
     * @param keyValue 校验后的密钥
     * @return 校验后正确的密钥
     * @throws IllegalArgumentException 字符串为空
     */
    private String checkKeyValue(String keyValue) {
        if (StringUtils.isBlank(keyValue)) {
            throw new IllegalArgumentException("key value can not be blank!");
        }
        // 不足16位的，在后面补0
        int length = keyValue.length();
        if (length >= DEFAULT_AES_KEY_VALUE.length()) {
            return keyValue.substring(0, DEFAULT_AES_KEY_VALUE.length());
        } else {
            return StringUtils.rightPad(keyValue, DEFAULT_AES_KEY_VALUE.length(), '0');
        }
    }
    
}