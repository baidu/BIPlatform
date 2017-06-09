define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,$out='';$out+='<tr class="report-line j-root-line" data-id="';
        $out+=$escape($data.id);
        $out+='">\n    <td>\n        <label class="ellipsis" title="';
        $out+=$escape($data.name);
        $out+='">';
        $out+=$escape($data.name);
        $out+='</label>\n    </td>\n    <td>\n        <span class="btn-has-icon btn-has-icon-edit c-p j-edit-data-sources-group">编辑</span>\n        <span class="btn-has-icon btn-has-icon-delete c-p j-del-data-sources-group">删除</span>\n    </td>\n</tr>';
        return $out;
    }
    return { render: anonymous };
});