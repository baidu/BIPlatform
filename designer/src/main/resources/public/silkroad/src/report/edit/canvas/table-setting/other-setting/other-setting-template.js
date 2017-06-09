define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<!-- 其他操作 -->\n<div class="data-format">\n    <div class="data-format-alone c-f">\n        <div class="data-format-black f-l">\n            <input type="checkbox" name="filterBlank" class="f-l c-p"\n                   ';
        if($data.filterBlank=="true" ){
        $out+=' checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2">过滤空白行</label>\n        </div>\n        <div class="data-format-black f-l">\n            <input type="checkbox" name="canChangedMeasure" class="f-l c-p" ';
        if($data.canChangedMeasure=="true"){
        $out+='\n                   checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2">显示富文本下拉框</label>\n        </div>\n        <div class="data-format-black f-l">\n            <input type="checkbox"  name="needSummary" class="f-l c-p" ';
        if($data.needSummary=="true"){
        $out+='\n                   checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2">显示合计行</label>\n        </div>\n        <div class="data-format-black f-l">\n            <input type="checkbox"  name="isShowZero" class="f-l c-p" ';
        if($data.isShowZero=="true"){
        $out+='\n                   checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2">无数据是否显示为0</label>\n        </div>\n\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});