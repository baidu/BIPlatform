package com.baidu.rigel.biplatform.ma.comm.util;

import java.util.List;
import java.util.function.Function;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;

public class ChorusIntegrateUtils {
    /**
     * chorus相关cookie信息key
     */
    public static final String CHORUS_USERNAME = "chorus.username";
    public static final String CHORUS_REFERRER = "chorus.referrer";
    public static final String CHORUS_SESSIONID = "chorus.sessionid";
    public static final String CHORUS_TOKEN = "chorus.token";

    /**
     * private constructor
     */
    private ChorusIntegrateUtils() {

    }

    /**
     * 判断当前请求是否从chorus处跳转而来的
     * 
     * @param cookies 请求的cookie
     * @param chorusInfoKey chorus跳转所带入的标识
     * @return 如果发现url的cookie符合既定接口，则返回true，否则返回false
     */
    public static boolean ifRequestFromChorus(List<Cookie> cookies, String chorusInfoKey) {
        String chorusInfoStr = getChorusInfoFromCookie(cookies, chorusInfoKey);
        return !StringUtils.isEmpty(chorusInfoStr);
    }

    /**
     * 从请求cookie中找出符合chorusInfoKey的cookie值
     * 
     * @param cookies 请求的cookie
     * @param chorusInfoKey chorus跳转所带入的标识
     * @return chorusInfoKey所对应的cookie值
     */
    public static String getChorusInfoFromCookie(List<Cookie> cookies, String chorusInfoKey) {
        String chorusInfo = null;
        Object[] tmp;
        tmp = cookies.stream().filter(cookie -> {
            return chorusInfoKey.equals(cookie.getName());
        }).map(getChorusInfo).toArray();

        if (tmp != null && tmp.length > 0) {
            chorusInfo = tmp[0].toString();
        }
        return chorusInfo;
    }

    /**
     * 得到实际的cookie值
     */
    private static Function<Cookie, String> getChorusInfo = cookie -> {
        return cookie.getValue();
    };
}
