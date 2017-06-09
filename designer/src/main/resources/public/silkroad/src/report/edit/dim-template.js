define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dimList=$data.dimList,$item=$data.$item,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div class="title">\r\n    维度\r\n    <!--<span class="icon-data-sources j-setting-dim-group" title="维度组管理"></span>-->\r\n</div>\r\n<div class="j-con-org-dim con-org-dim">\r\n    ';
        $each(dimList,function($item,$index){
        if($item.type !== "GROUP_DIMENSION"){
        $out+='\r\n    <div class="item hover-bg c-m j-root-line j-olap-element';
        if($item.canToInd){
        $out+=' j-can-to-ind';
        }
        if($item.type=="STANDARD_DIMENSION"){
        $out+=' j-org-dim';
        }
        $out+=' ';
        if($item.type=="TIME_DIMENSION"){
        $out+=' j-time-dim';
        }
        $out+=' ';
        if($item.type=="CALLBACK"){
        $out+=' j-callback-dim';
        }
        $out+='" data-id="';
        $out+=$escape($item.id);
        $out+='" data-name="';
        $out+=$escape($item.name);
        $out+='">\r\n        <span class="item-text ellipsis j-item-text">';
        $out+=$escape($item.caption);
        $out+='（';
        $out+=$escape($item.name);
        $out+='）</span>\r\n        <span class="icon-letter collect j-edit-dim-name" data-id="';
        $out+=$escape($item.id);
        $out+='" title="编辑名称">E</span>\r\n    </div>\r\n    ';
        }
        });
        $out+='\r\n</div>\r\n';
        $each(dimList,function($item,$index){
        if($item.type == "GROUP_DIMENSION"){
        $out+='\r\n<div class="item-group c-m j-dim-group" data-id="';
        $out+=$escape($item.id);
        $out+='">\r\n    <div class="group-title hover-bg j-olap-element j-group-title" title="编辑维度组" data-id="';
        $out+=$escape($item.id);
        $out+='" data-group="item-group">\r\n        <span class="icon-letter icon-fold j-icon-fold fs-18">－</span>\r\n        <span class="item-text ellipsis j-item-text">';
        $out+=$escape($item.caption);
        $out+='</span>\r\n        <span class="icon-letter icon-group j-edit-dim-group">E</span>\r\n    </div>\r\n    ';
        $each($item.levels,function($item,$index){
        $out+='\r\n    <div class="item c-m hover-bg j-root-line j-sub-dim" data-id="';
        $out+=$escape($item.id);
        $out+='">\r\n        <span class="item-text ellipsis j-item-text">';
        $out+=$escape($item.caption);
        $out+='（';
        $out+=$escape($item.name);
        $out+='）</span>\r\n        <span class="icon-letter collect j-delete-sub-dim" data-id="';
        $out+=$escape($item.id);
        $out+='" title="删除此项">D</span>\r\n    </div>\r\n    ';
        });
        $out+='\r\n</div>\r\n';
        }
        $out+=' ';
        });
        $out+='\r\n<div class="item ta-c">\r\n    <span class="btn-small br-3 j-add-dim-group-btn">添加维度组</span>\r\n    <input class="group-name j-add-dim-group-input hide" placeholder="输入维度组名，回车确定" type="text"/>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});