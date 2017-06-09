define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<!-- 其他操作 -->\n<div class="data-format">\n    <div class="data-format-alone c-f">\n        <div class="data-format-black f-l">\n            <input type="checkbox" name="isShowZero" class="f-l c-p" ';
        if($data.isShowZero=="true"){
        $out+='\n                   checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2">无数据是否显示为0</label>\n        </div>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});