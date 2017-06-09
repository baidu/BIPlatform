
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
package com.baidu.rigel.biplatform.parser.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月3日 
 * @since jdk 1.8 or after
 */
public class PropertiesUtil {
    
    
    /** 
     * toString
     * @param props
     * @param comment
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String toString(Properties props, String comment, String encoding)
            throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            props.store(baos, comment);
            baos.flush(); 
            return new String(baos.toByteArray(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (IOException e) {
            throw new Error("An IOException while working with byte array streams?!", e);
        } finally {
            IOUtils.closeQuietly(baos);
        }
    }
    
    
    
    /** 
     * toString default toString by utf8 encode
     * @param props
     * @param comment
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toString(Properties props, String comment)
            throws UnsupportedEncodingException {
        return toString(props, comment, "utf-8");
    }
    
    
    /** 
     * loadPropertiesFromCurrent
     * @param resource
     * @return
     * @throws IOException
     */
    public static Properties loadPropertiesFromPath(String resource) throws IOException {
        String location = resource;
        if (StringUtils.isBlank(location)) {
            location = "conf/default.properties";
        }
        Properties properties = new Properties();

        ClassLoader classLoaderToUse = Thread.currentThread().getContextClassLoader();

        URL url = classLoaderToUse != null ? classLoaderToUse.getResource(location) : ClassLoader
                    .getSystemResource(location);

        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();
        try {
            if (location != null && location.endsWith(".xml")) {
                properties.loadFromXML(is);
            } else {
                properties.load(is);
            }
        } finally {
            IOUtils.close(con);
            IOUtils.closeQuietly(is);
        }

        return properties;
    }
    
    
    public static Properties loadPropertiesFromFile(String file) throws IOException {
        String location = file;
        if (StringUtils.isBlank(location)) {
            throw new IllegalArgumentException("file can not be blank.");
        }
        Properties properties = new Properties();

        ClassLoader classLoaderToUse = Thread.currentThread().getContextClassLoader();

        URL url = classLoaderToUse != null ? classLoaderToUse.getResource(location) : ClassLoader
                    .getSystemResource(location);

        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();
        try {
            if (location != null && location.endsWith(".xml")) {
                properties.loadFromXML(is);
            } else {
                properties.load(is);
            }
        } finally {
            IOUtils.close(con);
            IOUtils.closeQuietly(is);
        }

        return properties;
    }

}

