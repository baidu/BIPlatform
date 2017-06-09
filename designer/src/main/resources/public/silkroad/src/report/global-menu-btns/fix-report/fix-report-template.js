define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,taskMgrList=$data.taskMgrList,$item=$data.$item,index=$data.index,$escape=$utils.$escape,$out='';$out+='<div class="fix-report-mgr c-f j-fix-report-mgr">\n    <div class="fix-report-tab">\n        <span class="fix-report-tab-focus">任务管理</span>\n        <span>任务监控</span>\n    </div>\n    <div class="fix-report-content c-f j-fix-report-content">\n        <div class="fix-report-task-mgr-list">\n            <table cellspacing="0">\n                <thead>\n                    <tr>\n                        <th class="list-index">任务名称</th>\n                        <th class="list-name">运行状态</th>\n                        <th class="list-btns">操作</th>\n                        <th class="list-url">url地址</th>\n                    </tr>\n                </thead>\n                <tbody>\n                    ';
        $each(taskMgrList,function($item,index){
        $out+='\n                    <tr class="task-item" task-id="';
        $out+=$escape($item.taskId);
        $out+='">\n                        <td><div class="list-index ellipsis" title="';
        $out+=$escape($item.taskName);
        $out+='">';
        $out+=$escape($item.taskName);
        $out+='</div></td>\n                        <td><div class="list-name ellipsis" title="';
        $out+=$escape($item.statusInfo);
        $out+='">';
        $out+=$escape($item.statusInfo);
        $out+='</div></td>\n                        <td>\n                            <div class="list-btns">\n                                <span class="biplt-radius biplt-start c-p j-task-start" title="启动">\n                                    <span></span>\n                                </span>\n                                <span class="biplt-normal biplt-look c-p j-task-look" title="查看"><span></span></span>\n                                <span class="biplt-radius biplt-radius-red biplt-del c-p j-task-del" title="删除"><span></span></span>\n                            </div>\n                        </td>\n                        <td><div class="list-url text c-p ellipsis j-task-url" title="';
        $out+=$escape($item.taskUrl);
        $out+='">';
        $out+=$escape($item.taskUrl);
        $out+='</div></td>\n                    </tr>\n                    ';
        });
        $out+='\n                </tbody>\n            </table>\n            ';
        if(taskMgrList.length === 0){
        $out+='\n            <div class="empty-data ta-c">暂无数据</div>\n            ';
        }
        $out+='\n        </div>\n        <div class="box-add c-f">\n            <span class="biplt-radius c-p biplt-add f-r j-task-add" title="新增任务">\n                <span></span>\n            </span>\n        </div>\n    </div>\n    <div class="f-r operation-btns j-operation-btns hide">\n        <span type="button" class="form-common-input-button mr-10 j-ok">完成</span>\n        <span type="button" class="form-common-input-button j-cancel">取消</span>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});