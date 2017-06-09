define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,indList=$data.indList,$value=$data.$value,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div class="title">\r\n    指标\r\n    <span class="icon-data-sources j-setting-derive-inds derivative-ind-setting" title="管理衍生指标"></span>\r\n</div>\r\n<div class="j-con-org-ind con-org-ind">\r\n    ';
        $each(indList.data,function($value,$index){
        $out+=' ';
        if($value.type == "COMMON" && $value.visible == true){
        $out+='\r\n    <div class="item c-m hover-bg j-root-line j-org-ind j-olap-element';
        if($value.canToDim){
        $out+=' j-can-to-dim';
        }
        $out+='" data-id="';
        $out+=$escape($value.id);
        $out+='">\r\n        <span class="item-text ellipsis j-item-text" title="';
        $out+=$escape($value.tag);
        $out+='（';
        $out+=$escape($value.name);
        $out+='）">\r\n            ';
        $out+=$escape($value.caption);
        $out+='（';
        $out+=$escape($value.name);
        $out+='）\r\n        </span>\r\n        <span class="icon-letter collect j-method-type" title="点击设置指标汇总方式">\r\n            ';
        $out+=$escape(indList.map[$value.aggregator]);
        $out+='\r\n        </span>\r\n    </div>\r\n    ';
        }
        });
        $out+='\r\n    ';
        $each(indList.data,function($value,$index){
        $out+=' ';
        if($value.type == "CALLBACK"){
        $out+='\r\n    <div class="item c-m hover-bg j-root-line j-org-ind j-olap-element" data-id="';
        $out+=$escape($value.id);
        $out+='">\r\n        <span class="item-text ellipsis j-item-text" title="';
        $out+=$escape($value.tag);
        $out+='（';
        $out+=$escape($value.name);
        $out+='）">\r\n            ';
        $out+=$escape($value.caption);
        $out+='（';
        $out+=$escape($value.name);
        $out+='）\r\n        </span>\r\n        <span class="icon-letter collect j-method-type" title="点击设置指标汇总方式">\r\n            ';
        $out+=$escape(indList.map[$value.aggregator]);
        $out+='\r\n        </span>\r\n    </div>\r\n    ';
        }
        });
        $out+='\r\n</div>\r\n';
        $each(indList.data,function($value,$index){
        if($value.type == "CAL" || $value.type == "RR" || $value.type == "SR"){
        $out+='\r\n<div class="item c-m hover-bg j-olap-element j-cal-ind" data-id="';
        $out+=$escape($value.id);
        $out+='">\r\n    <span class="item-text ellipsis fw-b" title="';
        $out+=$escape($value.caption);
        $out+='">';
        $out+=$escape($value.caption);
        $out+='</span>\r\n</div>\r\n';
        }
        });
        return $out;
    }
    return { render: anonymous };
});