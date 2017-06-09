define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,outParamDim=$data.outParamDim,$option=$data.$option,key=$data.key,$escape=$utils.$escape,selectDimId=$data.selectDimId,outParamLevel=$data.outParamLevel,selectLevel=$data.selectLevel,$out='';$out+='<!--\r\n数据例子：\r\nvar demoData = {\r\n    outParamDim: {\r\n        id: \'\',\r\n        caption: \'\',\r\n        name: \'\'\r\n    },\r\n    levelData: {\r\n        \'level1\': \'当前级别\',\r\n        \'level12\': \'下一级别\'\r\n    },\r\n    selectDimId: \'\',\r\n    selectDimName: \'\',\r\n    selectLevel: \'\'\r\n};\r\n-->\r\n<div class="comp-relation-event">\r\n    <span>选择被关联组件</span>\r\n    <div class="comp-realtion-box">\r\n    </div>\r\n    <span>选择传出参数</span>\r\n    <div class="comp-realtion-param">\r\n        <span class="span-out-param">选择传出维度</span>\r\n        <select class="j-comp-relation-event-out-param">\r\n            ';
        $each(outParamDim,function($option,key){
        $out+='\r\n            <option value="';
        $out+=$escape($option.id);
        $out+='$';
        $out+=$escape($option.name);
        $out+='" dimGroup="';
        if($option.dimGroup === true){
        $out+='true';
        }else{
        $out+='false';
        }
        $out+='"\r\n            ';
        if(selectDimId && $option.id === selectDimId){
        $out+='\r\n            selected="selected"\r\n            ';
        }
        $out+='>';
        $out+=$escape($option.caption);
        $out+='\r\n            </option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n        <span class="span-level">选择当前还是下一级</span>\r\n        <select class="j-comp-relation-event-out-param-level">\r\n            ';
        $each(outParamLevel,function($option,key){
        $out+='\r\n            <option value=';
        $out+=$escape(key);
        $out+='\r\n            ';
        if(selectLevel && key === selectLevel){
        $out+='\r\n            selected="selected"\r\n            ';
        }
        $out+='>';
        $out+=$escape($option);
        $out+='\r\n            </option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});