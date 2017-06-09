define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,indList=$data.indList,$ind=$data.$ind,name=$data.name,$escape=$utils.$escape,$out='';$out+='<!--\r\n数据例子：\r\nvar demoData = {\r\n    indList: {\r\n        click: {\r\n            caption: \'\',\r\n            axisName: \'\'\r\n        }\r\n    }\r\n};\r\n-->\r\n<!-- 指标颜色设置 -->\r\n<div class="dialog-content">\r\n    <div class="base-setting-box c-f j-axis-text-setting">\r\n        <span class="mb-20 f-l">请对坐标轴名字单独设置</span>\r\n        ';
        $each(indList,function($ind,name){
        $out+='\r\n        <div class="base-setting-item f-l c-f j-axis-text-item">\r\n            <span class="f-l">';
        $out+=$escape($ind.caption);
        $out+='：</span>\r\n            <input class="f-l" type="text" name="';
        $out+=$escape(name);
        $out+='" value="';
        $out+=$escape($ind.axisName);
        $out+='" placeholder="请不要超过26个字符" />\r\n            <span class="f-l error-msg cor-red j-error-msg hide">字符超过指定长度</span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});