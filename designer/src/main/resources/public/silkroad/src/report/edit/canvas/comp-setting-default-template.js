define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,reportCompId=$data.reportCompId,compId=$data.compId,compType=$data.compType,$each=$utils.$each,xAxis=$data.xAxis,item=$data.item,$index=$data.$index,yAxis=$data.yAxis,sAxis=$data.sAxis,$out='';$out+='<div class="con-comp-setting-type1 j-comp-setting" report-comp-id="';
        $out+=$escape(reportCompId);
        $out+='" data-comp-id="';
        $out+=$escape(compId);
        $out+='" data-comp-type="';
        $out+=$escape(compType);
        $out+='">\r\n    <div>\r\n        <div class="norm-empty-prompt table-norm-empty">纵轴指标信息不能为空</div>\r\n    </div>\r\n    <div class="data-axis-line data-axis-line-34 j-comp-setting-line j-line-x" data-axis-type="x">\r\n        <span class="letter">横轴:</span>\r\n        ';
        $each(xAxis,function(item,$index){
        $out+='\r\n        <div class="item hover-bg j-root-line c-m" data-id="';
        $out+=$escape(item.id);
        $out+='">\r\n            <span class="item-text j-item-text icon-font" title="';
        $out+=$escape(item.caption);
        $out+='（';
        $out+=$escape(item.name);
        $out+='）">\r\n            ';
        $out+=$escape(item.caption);
        if(item.name){
        $out+='（';
        $out+=$escape(item.name);
        $out+='）';
        }
        $out+='\r\n            </span>\r\n            <span class="icon hide j-delete" title="删除">×</span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <div class="data-axis-line data-axis-line-34 j-comp-setting-line j-line-y" data-axis-type="y">\r\n        <span class="letter">纵轴:</span>\r\n        ';
        $each(yAxis,function(item,$index){
        $out+='\r\n        <div class="item hover-bg j-root-line c-m" data-id="';
        $out+=$escape(item.id);
        $out+='">\r\n            ';
        if($data.compType === "CHART"){
        $out+='\r\n                ';
        if(item.chartType === null){
        $out+='\r\n                    <span class="icon-chart column j-icon-chart" chart-type="column" ></span>\r\n                ';
        }else{
        $out+='\r\n                    <span class="icon-chart ';
        $out+=$escape(item.chartType);
        $out+=' j-icon-chart" chart-type="';
        $out+=$escape(item.chartType);
        $out+='" ></span>\r\n                ';
        }
        $out+='\r\n            ';
        }
        $out+='\r\n            <span class="item-text j-item-text icon-font" title="';
        $out+=$escape(item.caption);
        $out+='（';
        $out+=$escape(item.name);
        $out+='）">\r\n            ';
        $out+=$escape(item.caption);
        if(item.name){
        $out+='（';
        $out+=$escape(item.name);
        $out+='）';
        }
        $out+='\r\n            </span>\r\n            <span class="icon hide j-delete" title="删除">×</span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <div class="data-axis-line data-axis-line-34 j-comp-setting-line j-line-s" data-axis-type="s">\r\n        <span class="letter">过滤轴:</span>\r\n        ';
        $each(sAxis,function(item,$index){
        $out+='\r\n        <div class="item hover-bg j-root-line" data-id="';
        $out+=$escape(item.id);
        $out+='">\r\n            <span class="item-text j-item-text" title="';
        $out+=$escape(item.caption);
        $out+='（';
        $out+=$escape(item.name);
        $out+='）">\r\n            ';
        $out+=$escape(item.caption);
        if(item.name){
        $out+='（';
        $out+=$escape(item.name);
        $out+='）';
        }
        $out+='\r\n            </span>\r\n            <span class="icon-letter j-delete" title="删除">×</span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <div class="data-axis-line data-axis-line-48 data-btn-line" style="margin: 5px 0 2px 35px;">\r\n        <span class="letter">设置:</span>\r\n        <span class="icon-letter icon-letter-btn j-set-data-format">数据格式</span>\r\n        <span class="icon-letter icon-letter-btn j-norm-info-depict">指标信息描述</span>\r\n        <span class="icon-letter icon-letter-btn j-set-relation">关联</span>\r\n        <span class="icon-letter icon-letter-btn j-set-text-align">文本对齐</span>\r\n        <span class="icon-letter icon-letter-btn j-set-link">跳转</span>\r\n        <span class="icon-letter icon-letter-btn j-others-operate">其他操作</span>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});