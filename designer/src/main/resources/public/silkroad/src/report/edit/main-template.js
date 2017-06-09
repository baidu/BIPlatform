define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,cubeList=$data.cubeList,$value=$data.$value,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div class="con-report-edit">\r\n    <div class="data-sources-setting overflow j-data-sources-setting">\r\n        <div class="overflow j-scroll-data-sources">\r\n            <div class="con-data-table p-r c-f">\r\n                <select class="data-table-select j-cube-select">\r\n                    ';
        $each(cubeList,function($value,$index){
        $out+='\r\n                    <option value="';
        $out+=$escape($value.id);
        $out+='">';
        $out+=$escape($value.name);
        $out+='</option>\r\n                    ';
        });
        $out+='\r\n                </select>\r\n                <span class="icon-data-sources j-icon-data-sources" title="数据模型相关设置"></span>\r\n            </div>\r\n            <div class="con-ind j-data-sources-setting-con-ind"></div>\r\n            <div class="con-dim j-data-sources-setting-con-dim"></div>\r\n        </div>\r\n    </div>\r\n    <div class="canvas j-canvas">\r\n        <div class="buttons btns-height j-global-btn"></div>\r\n        <div class="comp-setting j-con-comp-setting"></div>\r\n        <div class="report j-report"></div>\r\n        <div class="comp-menu j-global-menu"></div>\r\n        <span class="button button-flat-primary button-publish-report j-button-publish-report">发布</span>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});