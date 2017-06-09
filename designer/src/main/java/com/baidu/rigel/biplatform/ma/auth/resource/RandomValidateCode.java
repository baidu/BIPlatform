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
package com.baidu.rigel.biplatform.ma.auth.resource;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.resource.cache.CacheManagerForResource;

/**
 * Description:
 * 
 * @author david.wang
 *
 */
public final class RandomValidateCode {

    /**
     * 随机产生的字符串
     */
    private static String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(RandomValidateCode.class);
    
    /**
     * 随机数
     */
    private static Random random = new Random();
    /**
     * 干扰线数量
     */
    private static int lineSize = 40;
    
    /**
     * 随机产生字符数量
     */
    private static int stringNum = 4;

    /**
     * 图片宽
     */
    private static int width = 80;

    /**
     * 图片高
     */
    private static int height = 26;

    /**
     * 构造函数
     */
    private RandomValidateCode ( ) {
        
    }
    
    /*
     * 获得字体
     */
    private static Font getFont() {
        return new Font("Fixedsys", Font.CENTER_BASELINE, 18);
    }

    /*
     * 获得颜色
     */
    private static Color getRandColor(int fc, int bc) {
        final int baseNum = 255;
        if (fc > baseNum) {
            fc = baseNum;
        }
        if (bc > baseNum) {
            bc = baseNum;
        }
        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }

    /**
     * 
     * @param request
     * @param response
     * @param cacheManagerForResource 
     */
    public static void getRandcode(HttpServletRequest request, HttpServletResponse response, 
            CacheManagerForResource cacheManagerForResource) {
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics(); // 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 18));
        g.setColor(getRandColor(110, 133));
        // 绘制干扰线
        for (int i = 0; i <= lineSize; i++) {
            drowLine(g);
        }
        // 绘制随机字符
        String randomString = "";
        for (int i = 1; i <= stringNum; i++) {
            randomString = drowString(g, randomString, i);
        }
        String key = null;
        if (request.getCookies() != null) {
            for (Cookie tmp : request.getCookies()) {
                if (tmp.getName().equals(Constants.RANDOMCODEKEY)) {
                    key = tmp.getName();
                    cacheManagerForResource.removeFromCache(key);
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(key)) {
            key = String.valueOf(System.nanoTime());
        }
        cacheManagerForResource.setToCache(key, randomString);
        final Cookie cookie = new Cookie(Constants.RANDOMCODEKEY, key);
        cookie.setPath(Constants.COOKIE_PATH);
        response.addCookie(cookie);
        g.dispose();
        try {
            ImageIO.write(image, "JPEG", response.getOutputStream()); // 将内存中的图片通过流动形式输出到客户端
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }

    /*
     * 绘制字符串
     */
    private static String drowString(Graphics g, String randomString, int i) {
        g.setFont(getFont());
        g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
        String rand = String.valueOf(getRandomString(random.nextInt(randString.length())));
        randomString += rand;
        g.translate(random.nextInt(3), random.nextInt(3));
        g.drawString(rand, 13 * i, 16);
        return randomString;
    }

    /*
     * 绘制干扰线
     */
    private static void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /*
     * 获取随机的字符
     */
    public static String getRandomString(int num) {
        return String.valueOf(randString.charAt(num));
    }

}
