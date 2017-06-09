define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,oriInd=$data.oriInd,$value=$data.$value,$index=$data.$index,$escape=$utils.$escape,oriDim=$data.oriDim,$out='';$out+='<div class="data-sources-setting data-sources-set-show-data">\r\n    <div class="j-oriInd">\r\n        <div class="title">指标</div>\r\n        ';
        $each(oriInd,function($value,$index){
        $out+='\r\n        <div class="item ellipsis" title="';
        $out+=$escape($value.name);
        $out+='（';
        $out+=$escape($value.id);
        $out+='）">\r\n            <label><input class="checkbox" type="checkbox" ';
        if($value.selected){
        $out+='checked';
        }
        $out+=' value="';
        $out+=$escape($value.id);
        $out+='">';
        $out+=$escape($value.name);
        $out+='（';
        $out+=$escape($value.id);
        $out+='）</label>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <div class="j-oriDim">\r\n        <div class="title">维度</div>\r\n        ';
        $each(oriDim,function($value,$index){
        $out+='\r\n        <div class="item ellipsis" title="';
        $out+=$escape($value.name);
        $out+='（';
        $out+=$escape($value.id);
        $out+='）">\r\n            <label><input class="checkbox" type="checkbox" ';
        if($value.selected){
        $out+='checked';
        }
        $out+=' value="';
        $out+=$escape($value.id);
        $out+='">';
        $out+=$escape($value.name);
        $out+='（';
        $out+=$escape($value.id);
        $out+='）</label>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});