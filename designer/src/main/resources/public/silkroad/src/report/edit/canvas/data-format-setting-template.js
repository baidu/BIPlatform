define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,defaultFormat=$data.defaultFormat,$each=$utils.$each,options=$data.options,$option=$data.$option,optionKey=$data.optionKey,$escape=$utils.$escape,dataFormat=$data.dataFormat,$formatItem=$data.$formatItem,name=$data.name,$out='';$out+='<!--\r\n数据例子：\r\nvar demoData = {\r\n    options: {\r\n        \'I,III\': \'千分位整数（18,383）\',\r\n        \'I,III.DD\': \'千分位两位小数（18,383.88）\',\r\n        \'I.DD%\': \'百分比两位小数（34.22%）\',\r\n        \'HH:mm:ss\': \'时间（13:23:22）\',\r\n        \'D天HH:mm:ss\': \'时间格式（2天1小时23分45秒）\'\r\n    },\r\n    defaultFormat: \'I,III\',\r\n    dataFormat: {\r\n        \'cash\': {\r\n            caption: \'现金\',\r\n            format: \'I,III\'\r\n        },\r\n        \'crm\': {\r\n            caption: \'点击消费\',\r\n            format: \'I,III.dd\'\r\n        }\r\n    }\r\n};\r\n-->\r\n<div class="data-format">\r\n    <div class="data-format-default c-f">\r\n        <span title="指标默认数据格式">指标默认数据格式：</span>\r\n        <select name="defaultFormat">\r\n            <option value=""\r\n            ';
        if(!defaultFormat){
        $out+=' selected="selected" ';
        }
        $out+='>请选择</option>\r\n            ';
        $each(options,function($option,optionKey){
        $out+='\r\n            <option value=';
        $out+=$escape(optionKey);
        $out+='\r\n                ';
        if(defaultFormat && optionKey === defaultFormat){
        $out+='\r\n                    selected="selected"\r\n                ';
        }
        $out+='>';
        $out+=$escape($option);
        $out+='\r\n            </option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n    </div>\r\n    <div class="data-format-alone c-f">\r\n        <span>对各指标进行单独设置</span>\r\n        ';
        $each(dataFormat,function($formatItem,name){
        $out+='\r\n        <div class="data-format-alone-dim">\r\n            <span title="';
        $out+=$escape($formatItem.caption);
        $out+='">';
        $out+=$escape($formatItem.caption);
        $out+='：</span>\r\n            <select name="';
        $out+=$escape(name);
        $out+='">\r\n                <option value="" ';
        if(!$formatItem.format){
        $out+=' selected="selected" ';
        }
        $out+='>请选择</option>\r\n                ';
        $each(options,function($option,optionKey){
        $out+='\r\n                <option value=';
        $out+=$escape(optionKey);
        $out+='\r\n                ';
        if($formatItem.format && optionKey === $formatItem.format){
        $out+=' selected="selected"\r\n                ';
        }
        $out+='>';
        $out+=$escape($option);
        $out+='</option>\r\n                ';
        });
        $out+='\r\n            </select>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});