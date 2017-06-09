define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,item=$data.item,$out='';$out+='<div class="con-tab">\r\n    <!--<span class="item">静止时间</span>-->\r\n    <span class="item">动态时间</span>\r\n</div>\r\n<div class="con-tab-content">\r\n\r\n        <div class="item j-item" data-type="D">\r\n            <div class="title">开始时间日粒度设置</div>\r\n            <div class="content">\r\n                <input type="text" name="startDateSetting" value="';
        $out+=$escape(item.start);
        $out+='"/>\r\n                <select>\r\n                    <option value="D">日</option>\r\n                </select>\r\n            </div>\r\n            <div class="title">结束时间日粒度设置</div>\r\n            <div class="content">\r\n                <input type="text" name="endDateSetting" value="';
        $out+=$escape(item.end);
        $out+='"/>\r\n                <select>\r\n                    <option value="D">日</option>\r\n                </select>\r\n            </div>\r\n        </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});