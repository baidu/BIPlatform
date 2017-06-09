define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,appearance=$data.appearance,$escape=$utils.$escape,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    indList: {\n        click: {\n            caption: \'\',\n            axisName: \'\'\n        }\n    }\n};\n-->\n<!-- 指标颜色设置 -->\n<div class="dialog-content">\n    <div class="base-setting-box c-f j-appearance-setting">\n        <span class="f-l">外观设置</span>\n        <div class="base-setting-item f-l c-f w-p-100 ml-5 j-appearance-item">\n            <input type="checkbox" class="f-l c-p" name="isShowInds" ';
        if(appearance.isShowInds==true){
        $out+=' checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2 mt-1">显示指标区域</label>\n        </div>\n        <div class="base-setting-item f-l c-f w-p-100 ml-5 j-appearance-item">\n            <input type="checkbox" class="f-l c-p" name="isShowLegend" ';
        if(appearance.isShowLegend==true){
        $out+=' checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2 mt-1">显示图例</label>\n        </div>\n        <div class="base-setting-item f-l c-f w-p-100 ml-5 j-appearance-item">\n            <input type="checkbox" class="f-l c-p j-isShowTitle" name="isShowTitle" ';
        if(appearance.isShowTitle==true){
        $out+=' checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2 mt-1">显示标题</label>\n        </div>\n        <div class="base-setting-item f-l c-f w-p-100 ml-5 j-appearance-set-title ';
        if(appearance.isShowTitle!=true){
        $out+=' hide ';
        }
        $out+='">\n            <label class="f-l ml-2 mt-1">请输入标题</label>\n            <input type="text" class="f-l c-p input-text ml-5" name="chartTitle" value="';
        $out+=$escape(appearance.chartTitle);
        $out+='" />\n        </div>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});