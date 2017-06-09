define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,reportList=$data.reportList,$item=$data.$item,index=$data.index,$escape=$utils.$escape,$out='';$out+='<div class="j-root-report-list">\r\n    <div class="con-common-line">\r\n        <div class="con-common-max-min">\r\n            <span class="btn-has-icon btn-has-icon-new c-p j-add-report">新建报表</span>\r\n        </div>\r\n    </div>\r\n    <div class="con-report-list con-common-max-min">\r\n        <table cellspacing="0">\r\n            <thead>\r\n            <tr>\r\n                <th class="report-index">序号</th>\r\n                <th class="report-name">报表名称</th>\r\n                <th class="report-btns">操作按钮</th>\r\n            </tr>\r\n            </thead>\r\n            <tbody>\r\n            ';
        $each(reportList,function($item,index){
        $out+='\r\n            <tr class="report-line j-root-line" data-id="';
        $out+=$escape($item.id);
        $out+='" data-theme="';
        $out+=$escape($item.theme);
        $out+='">\r\n                <td>';
        $out+=$escape(index + 1);
        $out+='</td>\r\n                <td><a class="text c-p ellipsis j-show-report" title="点击预览">';
        $out+=$escape($item.name);
        $out+='</a>\r\n                </td>\r\n                <td>\r\n                    <span class="btn-has-icon btn-has-icon-copy c-p j-copy-report">创建副本</span>\r\n                    <span class="btn-has-icon btn-has-icon-info c-p j-info-report j-show-publish-info"\r\n                              title="报表的发布信息">查看发布信息</span>\r\n                    <span class="btn-has-icon btn-has-icon-edit c-p j-edit-report">编辑</span>\r\n                    <span class="btn-has-icon btn-has-icon-delete c-p j-delete-report">删除</span>\r\n                </td>\r\n            </tr>\r\n            ';
        });
        $out+='\r\n            </tbody>\r\n        </table>\r\n        ';
        if(reportList.length == 0){
        $out+='\r\n        <div class="empty-data ta-c">暂无数据</div>\r\n        ';
        }
        $out+='\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});