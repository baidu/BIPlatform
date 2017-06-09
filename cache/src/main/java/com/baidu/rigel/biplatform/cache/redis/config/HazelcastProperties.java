
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
package com.baidu.rigel.biplatform.cache.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


/** 
 *  hazelcast 的配置信息
 * @author xiaoming.chen
 * @version  2015年1月29日 
 * @since jdk 1.8 or after
 */
@ConfigurationProperties(prefix = "hazelcastServer")
public class HazelcastProperties {
    
    
    private String groupUserName = "Tesseract-hz";
    
    private String groupPassword= "tesseract";
    
    private String members = "127.0.0.1";
    
    private boolean portAutoIncrement = true;
    
    private ManCenter mancenter;
    
    private String instanceName= "tesseract-cluster";
    
    public static class ManCenter {
        private String url;
        private boolean enable = false;
        /** 
         * 获取 url 
         * @return the url 
         */
        public String getUrl() {
        
            return url;
        }
        /** 
         * 设置 url 
         * @param url the url to set 
         */
        public void setUrl(String url) {
        
            this.url = url;
        }
        /** 
         * 获取 enable 
         * @return the enable 
         */
        public boolean isEnable() {
        
            return enable;
        }
        /** 
         * 设置 enable 
         * @param enable the enable to set 
         */
        public void setEnable(boolean enable) {
        
            this.enable = enable;
        }
    }

    /** 
     * 获取 groupUserName 
     * @return the groupUserName 
     */
    public String getGroupUserName() {
    
        return groupUserName;
    }

    /** 
     * 设置 groupUserName 
     * @param groupUserName the groupUserName to set 
     */
    public void setGroupUserName(String groupUserName) {
    
        this.groupUserName = groupUserName;
    }

    /** 
     * 获取 groupPassword 
     * @return the groupPassword 
     */
    public String getGroupPassword() {
    
        return groupPassword;
    }

    /** 
     * 设置 groupPassword 
     * @param groupPassword the groupPassword to set 
     */
    public void setGroupPassword(String groupPassword) {
    
        this.groupPassword = groupPassword;
    }

    /** 
     * 获取 members 
     * @return the members 
     */
    public String getMembers() {
    
        return members;
    }

    /** 
     * 设置 members 
     * @param members the members to set 
     */
    public void setMembers(String members) {
    
        this.members = members;
    }

    /** 
     * 获取 portAutoIncrement 
     * @return the portAutoIncrement 
     */
    public boolean isPortAutoIncrement() {
    
        return portAutoIncrement;
    }

    /** 
     * 设置 portAutoIncrement 
     * @param portAutoIncrement the portAutoIncrement to set 
     */
    public void setPortAutoIncrement(boolean portAutoIncrement) {
    
        this.portAutoIncrement = portAutoIncrement;
    }

    /** 
     * 获取 mancenter 
     * @return the mancenter 
     */
    public ManCenter getMancenter() {
    
        return mancenter;
    }

    /** 
     * 设置 mancenter 
     * @param mancenter the mancenter to set 
     */
    public void setMancenter(ManCenter mancenter) {
    
        this.mancenter = mancenter;
    }

    /** 
     * 获取 instanceName 
     * @return the instanceName 
     */
    public String getInstanceName() {
    
        return instanceName;
    }

    /** 
     * 设置 instanceName 
     * @param instanceName the instanceName to set 
     */
    public void setInstanceName(String instanceName) {
    
        this.instanceName = instanceName;
    }
   
    
}

