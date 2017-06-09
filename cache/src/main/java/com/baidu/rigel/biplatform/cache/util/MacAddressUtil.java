
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
package com.baidu.rigel.biplatform.cache.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;

/** 
 *  
 * @author xiaoming.chen
 * @version  2015年3月9日 
 * @since jdk 1.8 or after
 */
public class MacAddressUtil {
    
    
    
    /** 
     * getMachineNetworkFlag 获取机器的MAC或者IP，优先获取MAC
     * @param ia
     * @return
     * @throws SocketException
     * @throws UnknownHostException
     */
    public static String getMachineNetworkFlag(InetAddress ia) throws SocketException, UnknownHostException {
        if(ia == null) {
            ia = InetAddress.getLocalHost();
        }
        String machineFlag = getMacAddress(ia);
        if(StringUtils.isBlank(machineFlag)) {
            machineFlag = getIpAddress(ia);
        }
        return machineFlag;
    }
    
    /** 
     * 获取指定地址的mac地址，不指定默认取本机的mac地址
     * @param ia
     * @throws SocketException
     * @throws UnknownHostException 
     */
    public static String getMacAddress(InetAddress ia) throws SocketException, UnknownHostException {
//        if(ia == null) {
//            ia = InetAddress.getLocalHost();
//        }
        //获取网卡，获取地址
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        StringBuffer sb = new StringBuffer("");
        if (mac != null) {
            for(int i=0; i<mac.length; i++) {
                if(i!=0) {
                    sb.append("-");
                }
                //字节转换为整数
                int temp = mac[i]&0xff;
                String str = Integer.toHexString(temp).toUpperCase();
                if(str.length()==1) {
                    sb.append("0"+str);
                }else {
                    sb.append(str);
                }
            }
        }
        return sb.toString();
    }
    
    /** 
     * 获取指定地址的ip地址，不指定默认取本机的ip地址
     * @param ia
     * @throws SocketException
     * @throws UnknownHostException 
     */
    private static String getIpAddress(InetAddress ia) throws UnknownHostException {
//        if(ia == null) {
//            ia = InetAddress.getLocalHost();
//        }
        return ia.getHostAddress();
    }

}

