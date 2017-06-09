define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dataSourcesList=$data.dataSourcesList,$dsGroup=$data.$dsGroup,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div>\r\n    <div class="con-set-cube c-f">\r\n        <div class="con-data-sources-list f-l j-root-data-sources-list">\r\n            <div class="title fs-14">请选择数据源</div>\r\n            ';
        $each(dataSourcesList,function($dsGroup,$index){
        $out+='\r\n             <div class="ellipsis btn-has-icon-data-sources-group" title="';
        $out+=$escape($dsGroup.name);
        $out+='" data-id="';
        $out+=$escape($dsGroup.id);
        $out+='">';
        $out+=$escape($dsGroup.name);
        $out+='</div>\r\n                ';
        if($dsGroup.active){
        $out+='\r\n                 <span class="btn-has-icon btn-has-icon-data-sources data-line c-p j-item';
        if($dsGroup.active.selected===true){
        $out+=' selected';
        }
        $out+='"\r\n                    data-id="';
        $out+=$escape($dsGroup.active.id);
        $out+='" group-id="';
        $out+=$escape($dsGroup.id);
        $out+='">';
        $out+=$escape($dsGroup.active.name);
        $out+='</span>\r\n                ';
        }
        $out+='\r\n            ';
        });
        $out+='\r\n            ';
        if(dataSourcesList.length == 0){
        $out+='\r\n            <div class="empty-data ta-c">\r\n                暂无数据源组\r\n                <a class="create-data-sources-link c-p td-u j-create-data-sources-link">\r\n                    现在去创建数据源组\r\n                </a>\r\n            </div>\r\n            ';
        }
        $out+='\r\n        </div>\r\n        <div class="con-cube-list f-l j-con-cube-list">\r\n            <div class="title fs-14">请选择要实用的数据表（可多选）</div>\r\n            <div class="empty-data ta-c">暂无数据表</div>\r\n        </div>\r\n    </div>\r\n    <div class="form-common-line ta-c">\r\n        ';
        if(dataSourcesList.length > 0){
        $out+='<span class="button button-flat-primary j-submit">提交</span>';
        }
        $out+='\r\n        <span class="button button-flat-primary j-cancel">取消</span>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});