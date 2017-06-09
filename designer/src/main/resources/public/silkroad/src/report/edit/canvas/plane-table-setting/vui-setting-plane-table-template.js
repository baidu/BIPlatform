define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,reportCompId=$data.reportCompId,compId=$data.compId,compType=$data.compType,$each=$utils.$each,yAxis=$data.yAxis,item=$data.item,$index=$data.$index,sAxis=$data.sAxis,$out='';$out+='<div class="con-comp-setting-type1 j-comp-setting" report-comp-id="';
        $out+=$escape(reportCompId);
        $out+='" data-comp-id="';
        $out+=$escape(compId);
        $out+='" data-comp-type="';
        $out+=$escape(compType);
        $out+='">\n    <div>\n        <div class="norm-empty-prompt table-norm-empty">纵轴指标信息不能为空</div>\n    </div>\n    <div class="data-axis-line data-axis-line-34 j-comp-setting-line j-line-y" data-axis-type="y">\n        <span class="letter">列:</span>\n        ';
        $each(yAxis,function(item,$index){
        $out+='\n        <div class="item hover-bg j-root-line c-m" data-id="';
        $out+=$escape(item.id);
        $out+='">\n            <span class="item-text j-item-text icon-font" title="';
        $out+=$escape(item.caption);
        $out+='（';
        $out+=$escape(item.name);
        $out+='）">\n            ';
        $out+=$escape(item.caption);
        if(item.name){
        $out+='（';
        $out+=$escape(item.name);
        $out+='）';
        }
        $out+='\n            </span>\n            <span class="icon hide j-delete" title="删除">×</span>\n        </div>\n        ';
        });
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-34 j-comp-setting-line j-line-s" data-axis-type="s">\n        <span class="letter">条件:</span>\n        ';
        $each(sAxis,function(item,$index){
        $out+='\n        <div class="item hover-bg j-root-line" data-id="';
        $out+=$escape(item.id);
        $out+='">\n            <span class="item-text j-item-text" title="';
        $out+=$escape(item.caption);
        $out+='（';
        $out+=$escape(item.name);
        $out+='）">\n            ';
        $out+=$escape(item.caption);
        if(item.name){
        $out+='（';
        $out+=$escape(item.name);
        $out+='）';
        }
        $out+='\n            </span>\n            <span class="icon-letter j-delete" title="删除">×</span>\n        </div>\n        ';
        });
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-48 data-btn-line" style="margin: 5px 0 2px 35px;">\n        <span class="letter">设置:</span>\n        <span class="icon-letter icon-letter-btn j-set-data-format">数据格式</span>\n        <span class="icon-letter icon-letter-btn j-norm-info-depict">指标信息描述</span>\n        <span class="icon-letter icon-letter-btn j-set-text-align">文本对齐</span>\n        <span class="icon-letter icon-letter-btn j-set-pagination">分页设置</span>\n        <span class="icon-letter icon-letter-btn j-others-operate">其他操作</span>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});