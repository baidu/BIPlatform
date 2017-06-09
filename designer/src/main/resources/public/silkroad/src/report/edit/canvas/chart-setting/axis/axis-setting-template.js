define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dim=$data.dim,data=$data.data,name=$data.name,$escape=$utils.$escape,$out='';$out+='<!--\r\n数据例子：\r\nvar demoData = {\r\n    measureId: \'\',\r\n    reocrdSize: \'\',\r\n    topType: \'\',\r\n    indList: [\r\n    ],\r\n    topTypeList: {\r\n        desc: bottom,\r\n        asc: top,\r\n        none: none\r\n    }\r\n};\r\n-->\r\n<div class="axis-setting">\r\n    <div class="axis-setting-title">请选择需要设定为副轴的指标</div>\r\n    <div class="axis-setting-dim">\r\n        ';
        $each(dim,function(data,name){
        $out+='\r\n            <div>';
        $out+=$escape(data.caption);
        $out+='\r\n                <input\r\n                type="checkbox"\r\n                class="axis-setting-checkbox"\r\n                name ="';
        $out+=$escape(data.name);
        $out+='"\r\n                ';
        if(data.axis === '1'){
        $out+='\r\n                checked="checked"\r\n                ';
        }
        $out+='\r\n                />\r\n            </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});