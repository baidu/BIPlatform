/**
 * xutil.file
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    文件相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  none
 */

(function () {
    
    var FILE = xutil.file;
            
    /**
     * 过滤文件名的非法字符
     * 只考虑了windows和linux
     * windows文件名非法字符：\/:*?"<>|
     * linux文件名非法字符：/
     */
    FILE.FILE_NAME_FORBIDEN_CHARACTER = {
        '\\' : '＼',
        '/' : '／',
        ':' : '：',
        '*' : '＊',
        '?' : '？', 
        '"' : '＂',
        '<' : '＜',
        '>' : '＞',
        '|' : '｜'
    };
    
    /**
     * 修正文件名
     * 只考虑了windows和linux，
     * 有些字符被禁止做文件名，用类似的字符（如对应的全角字符）替代。
     * 
     * @public
     * @param {string} name 日期对象
     * @return {string} 修正后的文件名
     */    
    FILE.fixFileName = function (name) {
        if (name == null) {
            return name;
        }
        return name.replace(
            /./g, 
            function (w) {
                return FILE.FILE_NAME_FORBIDEN_CHARACTER[w] || w;
            }
        );
    };
    
})();
