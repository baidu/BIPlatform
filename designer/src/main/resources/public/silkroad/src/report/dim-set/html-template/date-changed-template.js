define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,tableId=$data.tableId,$each=$utils.$each,currDims=$data.currDims,$dim=$data.$dim,$index=$data.$index,$escape=$utils.$escape,fields=$data.fields,$level=$data.$level,$field=$data.$field,dateFormatOptions=$data.dateFormatOptions,$format=$data.$format,$out='';if(tableId === "0" || tableId === "ownertable"){
        $out+='\r\n<!--内置维度-->\r\n<div class="date-relation-owner-two-part c-f j-date-two-part">\r\n    <span>选择时间字段：</span>\r\n    <select>\r\n        <option value="0">请选择</option>\r\n        ';
        $each(currDims,function($dim,$index){
        $out+='\r\n        <option value=';
        $out+=$escape($dim.name);
        $out+='>';
        $out+=$escape($dim.comment);
        $out+='\r\n        </option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n    <span>粒度：</span>\r\n    <select class="j-owner-date-level-select">\r\n        <option value="0">请选择</option>\r\n        ';
        $each(fields,function($level,$index){
        $out+='\r\n        <option value=';
        $out+=$escape($level.id);
        $out+='>';
        $out+=$escape($level.name);
        $out+='\r\n        </option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n    <span>时间格式：</span>\r\n    <select class="j-owner-date-type-select">\r\n        <option value="0">请选择</option>\r\n    </select>\r\n</div>\r\n';
        }else{
        $out+='\r\n<!--普通维度-->\r\n<div class="date-relation-normal-two-part c-f j-date-two-part">\r\n    <span>指定关联字段：</span>\r\n    <select>\r\n        <option value="0">请选择</option>\r\n        ';
        $each(currDims,function($dim,$index){
        $out+='\r\n        <option value=';
        $out+=$escape($dim.name);
        $out+='>';
        $out+=$escape($dim.comment);
        $out+='\r\n        </option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n    <span class="equal">=</span>\r\n    <select>\r\n        <option value="0">请选择</option>\r\n        ';
        $each(fields,function($field,$index){
        $out+='\r\n        <option value=';
        $out+=$escape($field.id);
        $out+='>';
        $out+=$escape($field.name);
        $out+='\r\n        </option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n</div>\r\n<div class="date-relation-normal-three-part j-date-three-part">\r\n    <span class="date-relation-normal-three-part-name">日期格式：</span>\r\n    <div class="date-relation-normal-three-part-box c-f">\r\n        ';
        $each(fields,function($field,$index){
        $out+='\r\n        <div class="date-relation-normal-three-part-box-date-format c-f">\r\n            <span>';
        $out+=$escape($field.name);
        $out+='</span>\r\n            <select formatKey=';
        $out+=$escape($field.id);
        $out+='>\r\n                <option value="0">请选择</option>\r\n                ';
        $each(dateFormatOptions[$field.id],function($format,$index){
        $out+='\r\n                <option value=';
        $out+=$escape($format);
        $out+='>';
        $out+=$escape($format);
        $out+='\r\n                </option>\r\n                ';
        });
        $out+='\r\n            </select>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n</div>\r\n';
        }
        $out+='\r\n\r\n\r\n';
        return $out;
    }
    return { render: anonymous };
});