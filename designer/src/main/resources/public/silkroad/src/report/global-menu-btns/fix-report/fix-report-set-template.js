define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,taskInfo=$data.taskInfo,$each=$utils.$each,paramList=$data.paramList,item=$data.item,$index=$data.$index,granularityId=$data.granularityId,$granularityKey=$data.$granularityKey,child=$data.child,$childKey=$data.$childKey,$out='';$out+='<div class="fix-report-basic-set j-fix-report-basic-set">\n    <h4>基本设置</h4>\n    <div class="fix-report-task-name-set c-f">\n        <span class="f-l">任务名称：</span>\n        <input class="f-l j-task-id" type="text" task-id="';
        $out+=$escape(taskInfo.taskId);
        $out+='" value="';
        $out+=$escape(taskInfo.taskName);
        $out+='"/>\n    </div>\n\n    <div class="fix-report-task-main-set">\n        <ul>\n            <li class="main-set-header c-f">\n                <span class="f-l first">参数名称</span>\n                <span class="f-l second">参数值</span>\n            </li>\n            ';
        $each(paramList,function(item,$index){
        $out+='\n            <li class="main-set-item c-f">\n                <span class="f-l">';
        $out+=$escape(item.caption);
        $out+='</span>\n                <ul class="item-sets f-l">\n                    <li>\n                        <div class="j-param-tree" data-param-name="';
        $out+=$escape(item.paramName);
        $out+='" data-param-caption="';
        $out+=$escape(item.caption);
        $out+='" data-param-id="';
        $out+=$escape(item.paramId);
        $out+='" data-tree-select-id="';
        $out+=$escape(item.paramValue.id);
        $out+='" data-tree-select-name="';
        $out+=$escape(item.paramValue.name);
        $out+='"></div>\n                    </li>\n                </ul>\n            </li>\n            ';
        });
        $out+='\n        </ul>\n    </div>\n</div>\n<div class="fix-report-scheduling">\n    <h4>调度设置</h4>\n    <div class="item-set-now-execute c-f">\n        <input type="checkbox" class="f-l j-isRunNow" ';
        if(taskInfo.isRunNow){
        $out+=' checked="checked" ';
        }
        $out+='/>\n        <span class="f-l">立即执行</span>\n    </div>\n    <div class="item-set-execute-time c-f j-isRunNow-set';
        if(taskInfo.isRunNow){
        $out+=' hide ';
        }
        $out+='">\n        <span class="f-l">执行时间:</span>\n        <div class="f-l c-f">\n            <input type="text" class="f-l j-time-hour" ';
        if(!taskInfo.isRunNow){
        $out+=' value="';
        $out+=$escape(taskInfo.executeStrategy.hour);
        $out+='" ';
        }
        $out+=' placeholder="请输入数字"/>\n            <span class="f-l">时</span>\n            <input type="text" class="f-l j-time-minute" ';
        if(!taskInfo.isRunNow){
        $out+=' value="';
        $out+=$escape(taskInfo.executeStrategy.minute);
        $out+='" ';
        }
        $out+=' placeholder="请输入数字"/>\n            <span class="f-l">分</span>\n        </div>\n    </div>\n    <div class="item-set-execute-granular c-f j-isRunNow-set';
        if(taskInfo.isRunNow){
        $out+=' hide ';
        }
        $out+='">\n        <span class="f-l">执行粒度:</span>\n        <div class="f-l c-f">\n            <select name="" id="" class="f-l j-granularity-parent">\n                ';
        if(taskInfo){
        $out+='\n                ';
        $each(taskInfo.granularityList.parent,function(granularityId,$granularityKey){
        $out+='\n                <option value="';
        $out+=$escape($granularityKey);
        $out+='" ';
        if($granularityKey===taskInfo.executeStrategy.granularity){
        $out+=' selected="selected" ';
        }
        $out+='>\n                ';
        $out+=$escape(granularityId);
        $out+='\n                </option>\n                ';
        });
        $out+='\n                ';
        }
        $out+='\n            </select>\n            <select name="" class="f-l j-granularity-child ';
        if(taskInfo.executeStrategy.granularity === 'D'){
        $out+='hide';
        }
        $out+='">\n                ';
        if(taskInfo.executeStrategy.granularity !== 'D'){
        $out+='\n                ';
        $each(taskInfo.granularityList.selectChild,function(child,$childKey){
        $out+='\n                <option value="';
        $out+=$escape($childKey);
        $out+='" ';
        if($childKey===taskInfo.executeStrategy.detail){
        $out+=' selected="selected" ';
        }
        $out+='>\n                ';
        $out+=$escape(child);
        $out+='\n                </option>\n                ';
        });
        $out+='\n                ';
        }
        $out+='\n            </select>\n        </div>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});