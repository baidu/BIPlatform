define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<div class="j-global-box">\n    <img class="j-global-add" src="src/css/img/add.png"/>\n    <div class="hide j-con-global-attr">\n        <div class="j-global-attr">\n            <input type="text" placeholder="维度名称"/>\n            <select>\n                <option>时间</option>\n                <option>地点</option>\n                <option>人物</option>\n            </select>\n            <div class="j-global-close"></div>\n        </div>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});