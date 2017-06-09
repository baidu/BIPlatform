define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,indList=$data.indList,$ind=$data.$ind,name=$data.name,$escape=$utils.$escape,$out='';$out+='<!--\r\n数据例子：\r\nvar demoData = {\r\n    indList: {\r\n        click: {\r\n            caption: \'\',\r\n            color: \'\'\r\n        }\r\n    }\r\n};\r\n-->\r\n<!-- 指标颜色设置 -->\r\n<div class="ind-color">\r\n    <div class="ind-color-alone">\r\n        <span>对各指标颜色进行单独设置</span>\r\n        ';
        $each(indList,function($ind,name){
        $out+='\r\n        <div class="ind-color-alone-ind">\r\n            <span>';
        $out+=$escape($ind.caption);
        $out+='：</span>\r\n            <input type="text" name="';
        $out+=$escape(name);
        $out+='" value="';
        $out+=$escape($ind.color);
        $out+='" />\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});