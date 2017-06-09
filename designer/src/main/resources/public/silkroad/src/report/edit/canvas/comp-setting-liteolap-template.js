define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,compId=$data.compId,$each=$utils.$each,xAxis=$data.xAxis,item=$data.item,$index=$data.$index,yAxis=$data.yAxis,sAxis=$data.sAxis,candInds=$data.candInds,candDims=$data.candDims,$out='';$out+='<div class="con-comp-setting-type1 j-comp-setting" data-comp-id="';
        $out+=$escape(compId);
        $out+='" data-comp-type="LITEOLAP">\n    <div>\n        <div class="norm-empty-prompt">纵轴指标信息不能为空</div>\n    </div>\n    <div class="data-axis-line data-axis-line-48 j-comp-setting-line j-line-x" data-axis-type="x">\n        <span class="letter">横轴:</span>\n        ';
        $each(xAxis,function(item,$index){
        $out+='\n        <div class="item hover-bg j-root-line c-m" data-id="';
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
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-48 j-comp-setting-line j-line-y" data-axis-type="y">\n        <span class="letter">纵轴:</span>\n        ';
        $each(yAxis,function(item,$index){
        $out+='\n        <div class="item hover-bg j-root-line c-m" data-id="';
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
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-48 j-comp-setting-line j-line-s" data-axis-type="s">\n        <span class="letter">过滤轴:</span>\n        ';
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
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-48 j-comp-setting-line j-line-cand-ind" data-axis-type="CAND_IND">\n        <span class="letter">候选指标:</span>\n        ';
        $each(candInds,function(item,$index){
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
        $out+='\n            </span>\n            ';
        if(!item.used){
        $out+='\n            <span class="icon-letter j-delete" title="删除">×</span>\n            ';
        }
        $out+='\n        </div>\n        ';
        });
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-48 j-comp-setting-line j-line-cand-dim" data-axis-type="CAND_DIM">\n        <span class="letter">候选维度:</span>\n        ';
        $each(candDims,function(item,$index){
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
        $out+='\n            </span>\n            ';
        if(!item.used){
        $out+='\n            <span class="icon-letter j-delete" title="删除">×</span>\n            ';
        }
        $out+='\n        </div>\n        ';
        });
        $out+='\n    </div>\n    <div class="data-axis-line data-axis-line-48 data-btn-line">\n        <span class="letter">设置:</span>\n        <span class="icon-letter icon-letter-btn j-set-data-format">数据格式</span>\n        <span class="icon-letter icon-letter-btn j-set-link">跳转</span>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});