/**
 * di.config.Lang
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    data insight 全局(包括console和product)的话术定义
 * @author:  xxx(xxx)
 */

$namespace('di.config');

(function() {
    
    //--------------------------------
    // 类型声明
    //--------------------------------

    var LANG = $namespace().Lang = {};
    var template = xutil.string.template;

    /**
     * ajax请求失败
     */
    LANG.AJAX_FAILURE = function (status) {
        return status + ' SERVER ERROR';
    };

    LANG.AJAX_TIMEOUT = '请求超时，请稍后重试';
    LANG.AJAX_WAITING = '请稍候';
    
    LANG.SMILE_FACE = '&nbsp;<div class="global-smile-face"></div>&nbsp;&nbsp;&nbsp;';
    LANG.SAD_FACE = '&nbsp;<div class="global-sad-face"></div>&nbsp;&nbsp;&nbsp;';
    
    LANG.OTHER_EDITING = function (otherEditing) {
        var stl = ' style="color: blue; font-weight: bold;" ';
        var tpl = [
            '<p>【注意】</p>',
            '<p>在#{0}页中，本报表已被编辑但未保存。</p>',
            '<p>如果确定保存，则会以<span ', stl, '>本编辑页</span>为准进行保存，放弃其他编辑页中做出的改动。确定保存吗？</p>'
        ];
        return template(
            tpl.join(''),
            ' "<span ' + stl + '>' + otherEditing.join('</span>", "<span ' + stl + '>') + '</span>" '
        );
    };
    LANG.SOME_ERROR = '抱歉，出现错误。';
    LANG.NEED_CREATE = '请先保存再执行此操作';
    LANG.OPT_SUCCESS = '操作成功';
    LANG.NO_SEL = '请选择';
    LANG.NO_DATA = '缺失数据';
    LANG.NO_AUTH = '抱歉，您没有查看当前页面的权限';
    LANG.NO_AUTH_OPERATION = '抱歉，您没有权限进行此操作';
    LANG.NO_AUTH_SYSTEM = '抱歉，您没有系统权限';
    LANG.ERROR = '系统异常';
    LANG.DATA_ERROR = '数据异常';
    LANG.ERROR_RTPL_ID = 'reportTemplateId错误或者不存在';
    LANG.RE_LOGIN = '请重新登陆';
    LANG.EMPTY_TEXT = '未查询到相关信息';
    LANG.QUERY_ERROR_TEXT = '查询数据出错，请检查';
    LANG.SAVE_FAIL = '抱歉，保存失败，请重试';
    LANG.SAVE_SUCCESS = '保存成功';
    LANG.PARAM_ERROR = '抱歉，参数校验失败';
    LANG.FATAL_DATA_ERROR = '抱歉，服务器异常，操作无法继续';
    
    LANG.INPUT_MANDATORY = '必填';
    LANG.INVALID_FORMAT = '格式错误';
    LANG.NUMBER_OVERFLOW = '数据过大';
    LANG.NUMBER_UNDERFLOW = '数据过小';
    LANG.TEXT_OVERFLOW = '输入文字过多';
    LANG.DOWNLOAD_FAIL = '下载失败';
    LANG.OFFLINE_DOWNLOAD_FAIL = '离线下载请求失败';
    LANG.DELETE_SUCCESS = '删除成功';

    LANG.GET_DIM_TREE_ERROR = '抱歉，维度数据获取失败，请重试';
    LANG.NEED_DS_ALL_LINKED = '请确保所有组件都有选择对应的数据集（没有则创建），再进行下一步';

    LANG.CONFIRM_ADD_SHARE = '您真的要添加分享吗？';
    LANG.CONFIRM_REMOVE_SHARE = '您真的要取消分享吗？';
    LANG.CONFIRM_DELETE = '您真的要删除吗？';

    LANG.DIM_MANDATORY = '请确认每种维度都有勾选，再点击查询';

    LANG.DESC_OVERFLOW = '解释说明文字过多';
    LANG.DESC_MANDATORY = '解释说明必填';
    LANG.PLAN_OVERFLOW = '跟进计划文字过多';
    LANG.PLAN_MANDATORY = '跟进计划必填';
    LANG.REASON_ADD_ERROR = '原因添加失败，请重试';

    LANG.DRILL_DIM_DATA_ERROR = '[维度数据校验失败]';

    LANG.WAITING_HTML = '<span class="waiting-icon"></span>&nbsp;<span class="waiting-text">加载中...</span>';

    //----------------------------------------------
    // 很丑陋地临时这么写：界面上显示的特殊的解释说明
    //----------------------------------------------

    LANG.TIME_DESC = [
        '<div style="border: 1px solid #BBB; padding: 10px;margin-top: 10px; border-radius: 5px;">',
            '<div style="font-weight: bold">&nbsp;&nbsp;时间表达式举例：</div>',
            '<br />',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;昨天：["-1D"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;六天前到当天：["-6D", "0D"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;本月初到当天：["0MB", "0D"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;本周末到2013-09-20：["0WE", "2013-09-20"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;上季初到明年末：["-1QB", "+1YE"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;后天到下月的后天：["+2D", null, "+1M"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;昨天所在的月的月初到昨天：[null, "-1D", "0MB"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;（注：字母用大小写都可以）</div>',
            '<br />',
            '<br />',
            '<div style="font-weight: bold">&nbsp;&nbsp;时间表达式说明：</div>',
            '<br />',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;时间表达式是一个 一元组 或者 二元组 或者 三元组</div>',
            '<div style="color: blue;">&nbsp;&nbsp;&nbsp;&nbsp;如：["0YB"] 或 ["2012-12-12", "5Q"] 或 ["-5ME", null, "6D"]</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;数组第一个元素表示开始时间，</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;绝对值（如2012-12-12）</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;或相对于基准时间的偏移（如-5d）</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;数组第二个元素表示结束时间，格式同上。（可缺省）</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;数组第三个元素表示时间区间，相对于start或end的偏移（如-4d）（可缺省）</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;如果已经定义了start和end，则range忽略。</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;如果start或end只有一个被定义，则range是相对于它的偏移。</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;如果只有start被定义，则只取start。</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;例如start是+1ME，range是+5WB，</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;表示一个时间范围：从下月的最后一天开始，到下月最后一天往后5周的周一为止。</div>',
            '<br />',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;三元组中每个元素的写法：</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;第一种情况是：</div>',
            '<div style="color: blue;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用YMDWQ（年月日周季）分别表示时间粒度（大小写都可以），</div>',
            '<div style="color: blue;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用B/E表示首尾，如果没有B/E标志则不考虑首尾</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;例如：</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;假如系统时间为2012-05-09</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+4D"表示系统时间往后4天，即2012-05-13 </div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"-2M"表示往前2个月（的当天），即2012-03-13</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"2Q"表示往后2个季度（的当天），即2012-11-13</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"1W"表示往后1周（的当天），即2012-05-20</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"1WB"表示往后1周的开头（周一），即2012-05-14</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"-1WE"表示往前一周的结束（周日），即2012-05-06</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"0WE"表示本周的结束（周日），即2012-05-13</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;月、季、年同理</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;第二种情况是：直接指定日期，如yyyy-MM-dd，</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;则返回此指定日期</div>',
            '<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可以用相对时间或绝对时间；</div>',
            '<br />',
        '</div>'   
    ].join(''); 

    LANG.X_CALENDAR_DESC = template([
        '<div style="border: 1px solid #BBB; padding: 10px;margin-top: 10px; border-radius: 5px;">',
            '<div style="font-weight: bold">&nbsp;&nbsp;设置初始化参数格式举例：</div>',
            '<br />',
            '<div>#{0}{</div>',
            '<div>#{0}#{0}"forbidEmpty": false,</div>',
            '<div>#{0}#{0}"disableCancelBtn": false,</div>',
            '<div>#{0}#{0}"timeTypeList": [</div>',
            '<div>#{0}#{0}#{0}{ "value": "D", "text": "日" },</div>',
            '<div>#{0}#{0}#{0}{ "value": "W", "text": "周" },</div>',
            '<div>#{0}#{0}#{0}{ "value": "M", "text": "月" },</div>',
            '<div>#{0}#{0}#{0}{ "value": "Q", "text": "季" }</div>',
            '<div>#{0}#{0}],</div>',
            '<div>#{0}#{0}"timeTypeOpt": {</div>',
            '<div>#{0}#{0}#{0}"D": {</div>',
            '<div>#{0}#{0}#{0}#{0}"selMode": "SINGLE",</div>',
            '<div>#{0}#{0}#{0}#{0}"date": ["-31D", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"range": ["2011-01-01", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"selModeList": [</div>',
            '<div>#{0}#{0}#{0}#{0}#{0}{ "text": "单选", "value": "SINGLE", "prompt": "单项选择" }</div>',
            '<div>#{0}#{0}#{0}#{0}]</div>',
            '<div>#{0}#{0}#{0}},</div>',
            '<div>#{0}#{0}#{0}"W": {</div>',
            '<div>#{0}#{0}#{0}#{0}"selMode": "RANGE",</div>',
            '<div>#{0}#{0}#{0}#{0}"date": ["-31D", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"range": ["2011-01-01", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"selModeList": [</div>',
            '<div>#{0}#{0}#{0}#{0}#{0}{ "text": "单选", "value": "SINGLE", "prompt": "单项选择" },</div>',
            '<div>#{0}#{0}#{0}#{0}#{0}{ "text": "范围多选", "value": "RANGE", "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值" }</div>',
            '<div>#{0}#{0}#{0}#{0}]</div>',
            '<div>#{0}#{0}#{0}},</div>',
            '<div>#{0}#{0}#{0}"M": {</div>',
            '<div>#{0}#{0}#{0}#{0}"selMode": "MULTIPLE",</div>',
            '<div>#{0}#{0}#{0}#{0}"date": ["-31D", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"range": ["2011-01-01", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"selModeList": [</div>',
            '<div>#{0}#{0}#{0}#{0}#{0}{ "text": "单选", "value": "SINGLE", "prompt": "单项选择" },</div>',
            '<div>#{0}#{0}#{0}#{0}#{0}{ "text": "范围多选", "value": "RANGE", "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值" }</div>',
            '<div>#{0}#{0}#{0}#{0}]</div>',
            '<div>#{0}#{0}#{0}},</div>',
            '<div>#{0}#{0}#{0}"Q": {</div>',
            '<div>#{0}#{0}#{0}#{0}"selMode": "SINGLE",</div>',
            '<div>#{0}#{0}#{0}#{0}"date": ["-31D", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"range": ["2011-01-01", "-1D"],</div>',
            '<div>#{0}#{0}#{0}#{0}"selModeList": [</div>',
            '<div>#{0}#{0}#{0}#{0}#{0}{ "text": "单选", "value": "SINGLE", "prompt": "单项选择" }</div>',
            '<div>#{0}#{0}#{0}#{0}]</div>',
            '<div>#{0}#{0}#{0}}</div>',
            '<div>#{0}#{0}}</div>',
            '<div>#{0}}</div>',
        '</div>'   
    ].join(''), Array(4).join('&nbsp;'));

})();