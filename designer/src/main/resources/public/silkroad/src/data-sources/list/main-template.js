define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dataSourcesGroupList=$data.dataSourcesGroupList,$dsGroup=$data.$dsGroup,index=$data.index,$escape=$utils.$escape,$ds=$data.$ds,$index=$data.$index,$out='';$out+='<div class="j-root-report-list">\r\n    <div class="con-common-line">\r\n        <div class="con-common-max-min">\r\n            <span class="btn-has-icon btn-has-icon-new c-p j-add-data-sources-group">新建数据源组</span>\r\n            <span class="btn-has-icon btn-has-icon-new c-p j-add-data-sources">新建数据源</span>\r\n        </div>\r\n    </div>\r\n    <div class="con-report-list con-common-max-min">\r\n        <table cellspacing="0">\r\n            <thead>\r\n            <tr>\r\n                <!--<th class="report-index">序号</th>-->\r\n                <th class="report-name">数据源名称</th>\r\n                <th class="data-sources-btns">操作按钮</th>\r\n            </tr>\r\n            </thead>\r\n            <tbody class="j-data-sources-tbody">\r\n            ';
        $each(dataSourcesGroupList,function($dsGroup,index){
        $out+='\r\n                <tr class="report-line j-root-line" data-id="';
        $out+=$escape($dsGroup.id);
        $out+='">\r\n                    <!--<td>-->\r\n                        <!--';
        $out+=$escape(index + 1);
        $out+='-->\r\n                    <!--</td>-->\r\n                    <td>\r\n                        <label class="ellipsis" title="';
        $out+=$escape($dsGroup.name);
        $out+='">';
        $out+=$escape($dsGroup.name);
        $out+='</label>\r\n                    </td>\r\n                    <td>\r\n                        <span class="btn-has-icon btn-has-icon-edit c-p j-edit-data-sources-group">编辑</span>\r\n                        <span class="btn-has-icon btn-has-icon-delete c-p j-del-data-sources-group">删除</span>\r\n                    </td>\r\n                </tr>\r\n                ';
        $each($dsGroup.dsList,function($ds,$index){
        $out+='\r\n                <tr class="report-line j-root-line" data-id="';
        $out+=$escape($ds.id);
        $out+='">\r\n                    <!--<td>-->\r\n                        <!--';
        $out+=$escape(index + 1);
        $out+='-->\r\n                    <!--</td>-->\r\n                    <td>\r\n                        <div class="data-source-item-name">\r\n                            <input type="radio" id="input-';
        $out+=$escape($ds.id);
        $out+='" class="j-input-data-sources" name="';
        $out+=$escape($dsGroup.id);
        $out+='" ';
        if($dsGroup.active === $ds.id){
        $out+='checked="checked"';
        }
        $out+='>\r\n                            <label class="ellipsis" title="';
        $out+=$escape($ds.name);
        $out+='" for="input-';
        $out+=$escape($ds.id);
        $out+='">';
        $out+=$escape($ds.name);
        $out+='</label>\r\n                        </div>\r\n\r\n                    </td>\r\n                    <td>\r\n                        <span class="btn-has-icon btn-has-icon-edit c-p j-edit-data-sources">编辑</span>\r\n                        <span class="btn-has-icon btn-has-icon-delete c-p j-delete-data-sources">删除</span>\r\n                    </td>\r\n                </tr>\r\n                ';
        });
        $out+='\r\n            ';
        });
        $out+='\r\n            </tbody>\r\n        </table>\r\n        ';
        if(dataSourcesGroupList.length == 0){
        $out+='\r\n        <div class="empty-data ta-c">暂无数据</div>\r\n        ';
        }
        $out+='\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});