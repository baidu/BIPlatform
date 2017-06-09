/**
 * xutil.date
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @change: 增加到秒粒度的日期format，增加方法#isValidFormatPattern。by MENGRAN at 2013-12-06
 * @file:   时间相关工具函数集合。
 *          便于工程中统一时间格式，并提供时间相关的数学操作。
 * @author: sushuang(sushuang)
 * @depend: xutil.lang, xutil.number
 */

(function () {
    /* globals xutil */
    var DATE = xutil.date;
    var LANG = xutil.lang;
    var COMMONUTIL = xutil.commonUtil;
        
    var DAY_MILLISECOND = 24*60*60*1000;
    
    /**
     * 默认通用的日期字符串格式为：
     * 'yyyy-MM-dd hh:mm'或'yyyy-MM-dd'或'yyyy-MM'或'yyyy'，
     * 如果要修改默认日期格式，修改如下诸属性。
     *
     * @type {string}
     * @public
     */
    DATE.DATE_FORMAT = 'yyyy-MM-dd';
    DATE.MINUTE_FORMAT = 'yyyy-MM-dd hh:mm';
    DATE.SECONDS_FORMAT = 'yyyy-MM-dd HH:mm:ss';

    /*
    *时间格式的格式化正则
    */
    DATE.TIME_REG = /h{1,2}:m{1,2}:s{1,2}$/i;
    
    /**
     * Add by MENGRAN at 2013-12-6
     * 判断是否为合法的格式化pattern
     * 
     * @public
     * @param {string} format 格式
     * @return {boolean} 是否合法
     */
    DATE.isValidFormatPattern = function (format) {
        if (format && 
            (format === DATE.DATE_FORMAT || format === DATE.MINUTE_FORMAT || format === DATE.SECONDS_FORMAT)) { return true; }
        return false;
    };

    /**
     * 日期对象转换成字符串的简写
     * 
     * @public
     * @param {Date} currDate 日期对象
     * @param {string} format 格式，缺省为yyyy-MM-dd
     * @return {string} 日期字符串
     */
    DATE.dateToString = function (date, format) {
        if (!date) { return ''; }
        format = format || DATE.DATE_FORMAT;
        return DATE.format(date, format);
    };
    
    /**
     * 日期对象转换成字符串的简写，到分钟精度
     * 
     * @public
     * @param {Date} date 日期对象
     * @param {string} format 格式，缺省为yyyy-MM-dd
     * @return {string} 日期字符串
     */
    DATE.dateToStringM = function (date) {
        return DATE.dateToString(date, DATE.MINUTE_FORMAT);
    };
    
    
    /**
     * 字符串转换成日期对象的简写
     * 
     * @public
     * @param {string} dateStr 字符串格式的日期，yyyy-MM-dd 或  yyyy-MM 或 yyyy
     * @return {Date} 日期对象，如果输入为空则返回null
     */
    DATE.stringToDate = function (dateStr) {
        if (dateStr) {
            return DATE.parse(dateStr);
        }
        return null;
    };
    
    /**
     * 得到昨天的日期对象
     * 
     * @public
     * @param {Date} date 目标日期对象
     * @return {Date} 结果
     */
    DATE.getYesterday = function (date) {
        if (!date) { return null; }
        return DATE.addDay(date, -1, true);
    };
    
    /**
     * 得到昨天的日期字符串
     * 
     * @public
     * @param {Date} date 目标日期对象
     * @return {string} 结果
     */
    DATE.getYesterdayString = function (date) {
        if (!date) { return null; }
        return DATE.dateToString(DATE.getYesterday(date));
    };
    
    /**
     * 得到周末
     * 
     * @public
     * @param {Date} date 目标日期对象
     * @param {boolean=} mode 
     *      true:得到星期六作为周末   false:得到星期日作为周末（默认）
     * @param {boolean=} remain 为false则新建日期对象（默认）；
     *                         为true则在输入的日期对象中改；
     *                         缺省为false
     */
    DATE.getWeekend = function (date, mode, remain) {
        var weekend = remain ? date : new Date(date);
        var offset = mode 
                ? (6 - weekend.getDay()) 
                : (7 - weekend.getDay()) % 7;
        weekend.setDate(weekend.getDate() + offset);
        return weekend;
    }
    
    /**
     * 得到周开始日期
     * 
     * @public
     * @param {Date} date 目标日期对象
     * @param {boolean=} mode 
     *      true:得到星期日作为周开始   false:得到星期一作为周开始（默认）
     * @param {boolean=} remain 为false则新建日期对象（默认）；
     *                         为true则在输入的日期对象中改；
     *                         缺省为false
     */
    DATE.getWorkday = function (date, mode, remain) {
        var workday = remain ? date : new Date(date);
        var d = workday.getDate();
        d = mode 
                ? (d - workday.getDay()) 
                : (d - (6 + workday.getDay()) % 7);
        workday.setDate(d);
        return workday;
    }
    
    /**
     * 获得某天是当前年的第几天
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @return {number} 结果天数
     */
    DATE.dateCountFromYearBegin = function (date) {
        if (!date) { return null; }
        LANG.isString(date) && (date = DATE.stringToDate(date)); 
        var startDate = new Date(date.getTime());
        startDate.setDate(1);
        startDate.setMonth(0);
        return DATE.dateMinus(date, startDate) + 1;
    };
    
    /**
     * 获得某天是当前季度的第几天
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @return {number} 结果天数
     */
    DATE.dateCountFromQuarterBegin = function (date) {
        if (!date) { return null; }
        LANG.isString(date) && (date = DATE.stringToDate(date)); 
        return DATE.dateMinus(date, DATE.getQuarterBegin(date)) + 1;
    };
    
    /**
     * 获得某天是当前月的第几天
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @return {number} 结果天数
     */
    DATE.dateCountFromMonthBegin = function (date) {
        if (!date) { return null; }
        LANG.isString(date) && (date = DATE.stringToDate(date)); 
        var startDate = new Date(date.getTime());
        startDate.setDate(1);
        return DATE.dateMinus(date, startDate) + 1;
    };
    
    /**
     * 获得某日期属于哪个季度，1~4
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @return {number} 季度号，1~4
     */
    DATE.getQuarter = function (date) {
        if (!date) { return null; }
        LANG.isString(date) && (date = DATE.stringToDate(date)); 
        return Math.floor(date.getMonth() / 3) + 1 ;
    };
    
    /**
     * 获得该季度的第一天
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @return {Date} 该季度的第一天
     */
    DATE.getQuarterBegin = function (date) {
        if (!date) { return null; }
        LANG.isString(date) && (date = DATE.stringToDate(date)); 
        var quarter = DATE.getQuarter(date);
        var mon = [0, 0, 3, 6, 9];
        return new Date(date.getFullYear(), mon[quarter], 1);
    };

    
    /**
     * 比较日期相同与否（两者有一者为空就认为是不同）
     * 
     * @public
     * @param {(string|Date)} date1 目标日期对象或日期字符串1
     * @param {(string|Date)} date2 目标日期对象或日期字符串2
     * @return {string} 比较结果
     */
    DATE.sameDate = function (date1, date2) {
        if (!date1 || !date2) { return false; }
        LANG.isString(date1) && (date1 = DATE.stringToDate(date1));
        LANG.isString(date2) && (date2 = DATE.stringToDate(date2));
        return date1.getFullYear() == date2.getFullYear() 
               && date1.getMonth() == date2.getMonth()
               && date1.getDate() == date2.getDate();
    };
    
    /**
     * 比较日期大小
     * 
     * @public
     * @param {(string|Date)} date1 目标日期对象或日期字符串1
     * @param {(string|Date)} date2 目标日期对象或日期字符串2
     * @return {string} 比较结果，
     *      -1: date1 < date2;  0: date1 == date2;  1: date1 > date2
     */
    DATE.compareDate = function (date1, date2) {
        var year1;
        var year2;
        var month1;
        var month2;
        var date1;
        var date2;

        LANG.isString(date1) && (date1 = DATE.stringToDate(date1));
        LANG.isString(date2) && (date2 = DATE.stringToDate(date2));
        if ((year1 = date1.getFullYear()) == (year2 = date2.getFullYear())) {
            if ((month1 = date1.getMonth()) == (month2 = date2.getMonth())) {
                if ((date1 = date1.getDate()) == (date2 = date2.getDate())) {
                    return 0;
                } 
                else { return date1 < date2 ? -1 : 1; }
            } 
            else { return month1 < month2 ? -1 : 1; }
        } 
        else { return year1 < year2 ? -1 : 1; }
    };
    
    /**
     * 用日做减法：date1 - date2
     * 如：date1为2012-03-13，date2为2012-03-15，则结果为-2。1.3天算2天。
     * 
     * @public
     * @param {(string|Date)} date1 目标日期对象或日期字符串1
     * @param {(string|Date)} date2 目标日期对象或日期字符串2
     * @return {string} 比较结果，
     *      -1: date1 < date2;  0: date1 == date2;  1: date1 > date2
     * @return {number} 减法结果天数
     */
    DATE.dateMinus = function (date1, date2) {
        // 格式化成一天最开始
        date1 = DATE.stringToDate(DATE.dateToString(date1)); 
        // 格式化成一天最开始
        date2 = DATE.stringToDate(DATE.dateToString(date2)); 
        var t = date1.getTime() - date2.getTime();
        var d = Math.round(t / DAY_MILLISECOND);
        return d;
    };
    
    /**
     * 增加天
     * 
     * @public
     * @param {Date} date 目标日期对象
     * @param {number} num 增加的天数，可为负数
     * @param {boolean} willNew 为true则新建日期对象；
     *                          为false则在输入的日期对象中改；
     *                          缺省为false
     * @return {Date} 结果
     */
    DATE.addDay = function (date, num, willNew) {
        if (!date) { return null; }
        num = num || 0;
        if (willNew) {
            return new Date(date.getTime() + num * DAY_MILLISECOND);
        } 
        else {
            date.setDate(date.getDate() + num);
            return date;
        }
    };
    
    /**
     * 增加月
     * 
     * @public
     * @param {Date} date 目标日期对象
     * @param {number} num 增加的月数，可为负数
     * @param {boolean} willNew 为true则新建日期对象；
     *                          为false则在输入的日期对象中改；
     *                          缺省为false
     * @return {Date} 结果
     */    
    DATE.addMonth = function (date, num, willNew) {
        if (!date) { return null; }
        num = num || 0;
        willNew && (date = new Date(date.getTime()));
        date.setMonth(date.getMonth() + num);
        return date;
    };  
    
    /**
     * 得到某日加num个月是几月
     * 
     * @public
     * @param {(string|Date)} date
     * @param {number} num 任意整数值，可以为负值
     * @return {Object} 
     *              {number} year 年
     *              {number} month 月号：1~12
     */
    DATE.nextMonth = function (date, num) {
        var year = date.getFullYear();
        var month = date.getMonth();
        return {
            year: year + Math.floor((month + num) / 12),
            month: (month + num + Math.abs(num * 12)) % 12 + 1
        }
    };
    
    /**
     * 得到某日加num个季度是几季度
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @param {number} num 任意整数值，可为负值
     * @return {Object} 
     *              {number} year 年
     *              {number} quarter 季度号：1~4
     */
    DATE.nextQuarter = function (date, num) {
        if (!date) { return null; }
        LANG.isString(date) && (date = DATE.stringToDate(date));

        var quarter = DATE.getQuarter(date);
        var year = date.getFullYear();
        return {
            year: year + Math.floor((quarter - 1 + num) / 4),
            quarter: (quarter - 1 + num + Math.abs(num * 4)) % 4 + 1
        };
    };
    
    /**
     * 返回某日的星期几字符串
     * 
     * @public
     * @param {(string|Date)} date 目标日期
     * @param {string} weekPrefix 星期几字符串前缀，缺省为'周'
     * @return {string} 星期几字符串
     */
    DATE.getDay = function (date, weekPrefix) {
        if (!date) { return ''; }
        LANG.isString(date) && (date = DATE.stringToDate(date));
        weekPrefix = weekPrefix || '周';
        var ret;
        switch (date.getDay()) {
            case 1: ret = weekPrefix + '一'; break;
            case 2: ret = weekPrefix + '二'; break;
            case 3: ret = weekPrefix + '三'; break;
            case 4: ret = weekPrefix + '四'; break;
            case 5: ret = weekPrefix + '五'; break;
            case 6: ret = weekPrefix + '六'; break;
            case 0: ret = weekPrefix + '日'; break;
            default: ret = ''; break;
        }
        return ret;
    };
    
    /**
     * 对目标日期对象进行格式化 (@see tangram)
     * 格式表达式，变量含义：
     * hh: 带 0 补齐的两位 12 进制时表示
     * h: 不带 0 补齐的 12 进制时表示
     * HH: 带 0 补齐的两位 24 进制时表示
     * H: 不带 0 补齐的 24 进制时表示
     * mm: 带 0 补齐两位分表示
     * m: 不带 0 补齐分表示
     * ss: 带 0 补齐两位秒表示
     * s: 不带 0 补齐秒表示
     * yyyy: 带 0 补齐的四位年表示
     * yy: 带 0 补齐的两位年表示
     * MM: 带 0 补齐的两位月表示
     * M: 不带 0 补齐的月表示
     * dd: 带 0 补齐的两位日表示
     * d: 不带 0 补齐的日表示
     * 
     * @public
     * @param {Date} source 目标日期对象
     * @param {string} pattern 日期格式化规则
     * @return {string} 格式化后的字符串
     */
    DATE.format = function (source, pattern) {
        var pad = COMMONUTIL.pad;
        if (!LANG.isString(pattern)) {
            return source.toString();
        }
    
        function replacer(patternPart, result) {
            pattern = pattern.replace(patternPart, result);
        }
        
        var year    = source.getFullYear();
        var month   = source.getMonth() + 1;
        var date2   = source.getDate();
        var hours   = source.getHours();
        var minutes = source.getMinutes();
        var seconds = source.getSeconds();
    
        replacer(/yyyy/g, pad(year, 4));
        replacer(/yy/g, pad(parseInt(year.toString().slice(2), 10), 2));
        replacer(/MM/g, pad(month, 2));
        replacer(/M/g, month);
        replacer(/dd/g, pad(date2, 2));
        replacer(/d/g, date2);
    
        replacer(/HH/g, pad(hours, 2));
        replacer(/H/g, hours);
        replacer(/hh/g, pad(hours % 12, 2));
        replacer(/h/g, hours % 12);
        replacer(/mm/g, pad(minutes, 2));
        replacer(/m/g, minutes);
        replacer(/ss/g, pad(seconds, 2));
        replacer(/s/g, seconds);
    
        return pattern;
    };

    /**
    *对目标数字进行格式化成 小时：分钟：秒
    *@public
    *@param source 目标数字
    *
    **/
    DATE.formatTime = function(source,pattern){
        var pad = COMMONUTIL.pad;
        if (!LANG.isString(pattern)) {
            return source.toString();
        }

        var hour = parseInt(source /3600);

        var day = parseInt(hour / 24);
        hour = parseInt(hour%24);
        var min = parseInt(source%3600 /60);
        var sec = Math.round(source % 60);
        if(day == 0){
            pattern = 'hh:mm:ss';
        }
        function replacer(patternPart, result) {
            pattern = pattern.replace(patternPart, result);
        }

        replacer(/d/ig,day);
        replacer(/hh/ig, pad(hour, 2));
        replacer(/h/ig, hour);
        replacer(/mm/ig, pad(min, 2));
        replacer(/m/ig, min);
        replacer(/ss/ig, pad(sec, 2));
        replacer(/s/ig, sec);
        return pattern;
    }
    
    
    /**
     * 将目标字符串转换成日期对象 (@see tangram)
     * 对于目标字符串，下面这些规则决定了 parse 方法能够成功地解析：
     * 短日期可以使用“/”或“-”作为日期分隔符，但是必须用月/日/年的格式来表示，例如"7/20/96"。
     * 以 "July 10 1995" 形式表示的长日期中的年、月、日可以按任何顺序排列，年份值可以用 2 位数字表示也可以用 4 位数字表示。如果使用 2 位数字来表示年份，那么该年份必须大于或等于 70。
     * 括号中的任何文本都被视为注释。这些括号可以嵌套使用。
     * 逗号和空格被视为分隔符。允许使用多个分隔符。
     * 月和日的名称必须具有两个或两个以上的字符。如果两个字符所组成的名称不是独一无二的，那么该名称就被解析成最后一个符合条件的月或日。例如，"Ju" 被解释为七月而不是六月。
     * 在所提供的日期中，如果所指定的星期几的值与按照该日期中剩余部分所确定的星期几的值不符合，那么该指定值就会被忽略。例如，尽管 1996 年 11 月 9 日实际上是星期五，"Tuesday November 9 1996" 也还是可以被接受并进行解析的。但是结果 date 对象中包含的是 "Friday November 9 1996"。
     * JScript 处理所有的标准时区，以及全球标准时间 (UTC) 和格林威治标准时间 (GMT)。 
     * 小时、分钟、和秒钟之间用冒号分隔，尽管不是这三项都需要指明。"10:"、"10:11"、和 "10:11:12" 都是有效的。
     * 如果使用 24 小时计时的时钟，那么为中午 12 点之后的时间指定 "PM" 是错误的。例如 "23:15 PM" 就是错误的。 
     * 包含无效日期的字符串是错误的。例如，一个包含有两个年份或两个月份的字符串就是错误的。
     *             
     * @public
     * @param {string} source 目标字符串
     * @return {Date} 转换后的日期对象
     */
    DATE.parse = function (source) {
        var reg = new RegExp("^\\d+(\\-|\\/)\\d+(\\-|\\/)\\d+\x24");
        if ('string' == typeof source) {
            if (reg.test(source) || isNaN(Date.parse(source))) {
                var d = source.split(/ |T/);
                var d1 = d.length > 1 
                        ? d[1].split(/[^\d]/)
                        : [0, 0, 0];
                var d0 = d[0].split(/[^\d]/);
                
                return new Date(
                    d0[0],
                    (d0[1] != null ? (d0[1] - 1) : 0 ), 
                    (d0[2] != null ? d0[2] : 1), 
                    (d1[0] != null ? d1[0] : 0), 
                    (d1[1] != null ? d1[1] : 0), 
                    (d1[2] != null ? d1[2] : 0)
                );
            } 
            else {
                return new Date(source);
            }
        }
        
        return new Date();
    };

})();
