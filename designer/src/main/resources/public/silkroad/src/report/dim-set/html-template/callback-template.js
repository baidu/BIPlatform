define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dim=$data.dim,$cube=$data.$cube,i=$data.i,$escape=$utils.$escape,cubes=$data.cubes,j=$data.j,$dim=$data.$dim,$index=$data.$index,$out='';$out+='<div class="dim-container-callback hide j-callback-main">\r\n    <ul class="callback-column-names c-f">\r\n        <li class="callback-column-names-main-table"><span>主数据表</span></li>\r\n        <li class="callback-column-names-setting"><span>配置区</span></li>\r\n    </ul>\r\n    ';
        $each(dim.callback,function($cube,i){
        $out+='\r\n    <div class="callback-main-box c-f j-callback-main-box" bodyIndex=';
        $out+=$escape(i);
        $out+='>\r\n        <span class="cube-name" cubeId=';
        $out+=$escape($cube.cubeId);
        $out+=' title=';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='>';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='</span>\r\n        <span class="callback-cube-open j-callback-cube-open"></span>\r\n        <div class="callback-relation-container c-f">\r\n            ';
        $each($cube.children,function($line,j){
        $out+='\r\n            <div class="callback-relation-box c-f j-callback-relation-box" bodyIndex=';
        $out+=$escape(j);
        $out+='>\r\n                ';
        if(j !==0 ){
        $out+='\r\n                <span class="callback-broken-line"></span>\r\n                ';
        }
        $out+='\r\n                <div class="callback-relation-content">\r\n                    <div class="first-part c-f">\r\n                        <span>选择回调字段：</span>\r\n                        <select>\r\n                            <option value="0">请选择</option>\r\n                            ';
        $each(cubes[$cube.cubeId].currDims,function($dim,$index){
        $out+='\r\n                            <option value=';
        $out+=$escape($dim.id);
        $out+='\r\n                            ';
        if($dim.id === $line.currDim){
        $out+='selected="selected"\r\n                            ';
        }
        $out+='>';
        $out+=$escape($dim.name);
        $out+='\r\n                            </option>\r\n                            ';
        });
        $out+='\r\n                        </select>\r\n                    </div>\r\n                    <div class="callback-relation-content-two-part c-f">\r\n                        <span class="callback-address-name">填写回调地址：</span>\r\n                        <input type="text" name="" id="" class="callback-address-input j-callback-address-input" value="';
        $out+=$escape($line.address);
        $out+='" />\r\n                        <span class="callback-address-prompt">\r\n                            例如：http://10.46.133.66:8999/pfplat/callbackmock.action\r\n                        </span>\r\n                    </div>\r\n                    <div class="callback-relation-content-three-part c-f">\r\n                        <span class="callback-cache-name">选取缓存类型：</span>\r\n                        <div>\r\n                            <input type="radio" value="1" class="callback-cache-type-right-input " name="callback-cache-body';
        $out+=$escape(i);
        $out+='-box';
        $out+=$escape(j);
        $out+='"\r\n                            ';
        if($line.refreshType === 1){
        $out+=' checked="checked" ';
        }
        $out+=' />\r\n                            <label class="callback-cache-type-right-label">\r\n                                无需缓存（数据量大，不推荐）\r\n                            </label>\r\n                        </div>\r\n                        <div>\r\n                            <input type="radio" value="2" class="callback-cache-type-right-input" name="callback-cache-body';
        $out+=$escape(i);
        $out+='-box';
        $out+=$escape(j);
        $out+='"\r\n                            ';
        if($line.refreshType === 2){
        $out+=' checked="checked" ';
        }
        $out+=' />\r\n                            <label class="callback-cache-type-right-label">\r\n                                在数据刷新后立刻刷新缓存\r\n                            </label>\r\n                        </div>\r\n                        <div class="ml-89">\r\n                            <input type="radio" value="3" class="callback-cache-type-right-input" name="callback-cache-body';
        $out+=$escape(i);
        $out+='-box';
        $out+=$escape(j);
        $out+='"\r\n                            ';
        if($line.refreshType === 3){
        $out+=' checked="checked" ';
        }
        $out+='/>\r\n                                <span class="callback-cache-type-right-label">间隔\r\n                                <input type="text" class="callback-cache-type-interval j-callback-cache-type-interval" value="';
        $out+=$escape($line.interval);
        $out+='" />\r\n                                秒刷新一次缓存</span>\r\n                        </div>\r\n                    </div>\r\n                </div>\r\n                <span class="delete j-callback-delete"></span>\r\n                ';
        if(j === ($cube.children.length-1)){
        $out+='\r\n                <span class="add j-callback-add"></span>\r\n                ';
        }
        $out+='\r\n            </div>\r\n            ';
        });
        $out+='\r\n        </div>\r\n        <span class="callback-error-msg j-callback-error-msg hide"></span>\r\n    </div>\r\n    ';
        });
        $out+='\r\n</div>\r\n\r\n';
        return $out;
    }
    return { render: anonymous };
});