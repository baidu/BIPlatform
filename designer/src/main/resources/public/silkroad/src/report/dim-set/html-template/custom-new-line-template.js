define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,$out='';$out+=' <div class="custom-create-new-dim-box j-custom-relation-box">\r\n    <div class="custom-create-new-dim-texts">\r\n        <input type="text"  placeholder="请输入新维度的名称" value=""/>\r\n        <textarea placeholder="请输入创建维度的逻辑语句" class="j-custom-sql" bodyIndex=';
        $out+=$escape($data);
        $out+='></textarea>\r\n        <span class="custom-create-new-dim-texts-wrong"></span>\r\n    </div>\r\n    <span class="delete j-custom-delete"></span>\r\n    <span class="add j-custom-add"></span>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});