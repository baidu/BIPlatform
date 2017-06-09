define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,compId=$data.compId,$each=$utils.$each,xAxis=$data.xAxis,$dim=$data.$dim,$index=$data.$index,$out='';$out+='<div class="con-comp-setting-type1 j-comp-setting" data-comp-id="';
        $out+=$escape(compId);
        $out+='" data-comp-type="TIME_COMP">\r\n    <div class="data-axis-line data-axis-line-48 j-comp-setting-line j-line-x" data-axis-type="x">\r\n        <span class="letter">时间维度:</span>\r\n        ';
        $each(xAxis,function($dim,$index){
        $out+='\r\n        <div class="item hover-bg c-m j-root-line  ui-draggable j-can-to-ind ui-draggable-dragging olap-element-dragging" data-id="';
        $out+=$escape($dim.id);
        $out+='" data-name="';
        $out+=$escape($dim.name);
        $out+='">\r\n            <span class="item-text j-item-text icon-font">\r\n            ';
        $out+=$escape($dim.caption);
        $out+='（';
        $out+=$escape($dim.name);
        $out+='）\r\n            </span>\r\n            <span class="icon hide j-delete" title="删除">×</span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <div class="data-axis-line data-axis-line-48 data-btn-line">\r\n        <span class="letter">设置:</span>\r\n        <span class="icon-letter icon-letter-btn j-set-default-time">默认选中时间</span>\r\n        <span>时间选择类型：</span>\r\n        <select class="select-calendar-type" data-comp-id="';
        $out+=$escape(compId);
        $out+='" data-comp-type="TIME_COMP">\r\n            <option value="CAL_SELECT" ';
        if($data.compMold && $data.compMold==="CAL_SELECT"){
        $out+=' selected="selected"';
        }
        $out+='>\r\n            时间单选\r\n            </option>\r\n            <option value="DOUBLE_CAL_SELECT" ';
        if($data.compMold && $data.compMold==="DOUBLE_CAL_SELECT"){
        $out+=' selected="selected"';
        }
        $out+='>\r\n            时间双选\r\n            </option>\r\n        </select>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});