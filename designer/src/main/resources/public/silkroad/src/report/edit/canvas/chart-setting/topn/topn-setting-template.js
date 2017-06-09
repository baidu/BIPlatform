define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,measureId=$data.measureId,$each=$utils.$each,indList=$data.indList,$ind=$data.$ind,$index=$data.$index,$escape=$utils.$escape,topTypeList=$data.topTypeList,$option=$data.$option,key=$data.key,topType=$data.topType,recordSize=$data.recordSize,$out='';$out+='<!--\r\n数据例子：\r\nvar demoData = {\r\n    measureId: \'\',\r\n    reocrdSize: \'\',\r\n    topType: \'\',\r\n    indList: [\r\n\r\n    ],\r\n    topTypeList: {\r\n        desc: bottom,\r\n        asc: top,\r\n        none: none\r\n    }\r\n};\r\n-->\r\n<div class="topn-indlist">\r\n    <div class="topn-indlist-item">\r\n        <span>指标选择：</span>\r\n        <select name="measureId">\r\n            <option value=""\r\n            ';
        if(!measureId){
        $out+=' selected="selected" ';
        }
        $out+='>请选择</option>\r\n            ';
        $each(indList,function($ind,$index){
        $out+='\r\n            <option value=';
        $out+=$escape($ind.id);
        $out+='\r\n            ';
        if(measureId && $ind.id === measureId){
        $out+=' selected="selected" ';
        }
        $out+='>';
        $out+=$escape($ind.caption);
        $out+='\r\n            </option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n    </div>\r\n    <div class="topn-indlist-item">\r\n        <span>排序方式：</span>\r\n        <select name="topType">\r\n            ';
        $each(topTypeList,function($option,key){
        $out+='\r\n            <option value=';
        $out+=$escape(key);
        $out+='\r\n            ';
        if(topType && key === topType){
        $out+='\r\n            selected="selected"\r\n            ';
        }
        $out+='>';
        $out+=$escape($option);
        $out+='\r\n            </option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n    </div>\r\n    <div class="topn-indlist-item">\r\n        <span>请输入条数：</span>\r\n        <input type="text" name="recordSize" value="';
        $out+=$escape(recordSize);
        $out+='"/>\r\n    </div>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});