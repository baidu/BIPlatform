define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,list=$data.list,item=$data.item,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div class="con-tab">\r\n    <!--<span class="item">静止时间</span>-->\r\n    <span class="item">动态时间</span>\r\n</div>\r\n<div class="con-tab-content">\r\n    ';
        $each(list,function(item,$index){
        $out+='\r\n    ';
        if(item.type == 'D'){
        $out+='\r\n    <div class="item j-item" data-type="D">\r\n        <div class="title">日粒度设置</div>\r\n        <div class="content">\r\n            <input type="text" name="singleDateSetting" value="';
        $out+=$escape(item.defaultSelectedVal);
        $out+='"/>\r\n            <select>\r\n                <option value="D">日</option>\r\n            </select>\r\n        </div>\r\n    </div>\r\n    ';
        }
        $out+='\r\n\r\n    ';
        if(item.type == 'W'){
        $out+='\r\n    <div class="item j-item" data-type="W">\r\n        <div class="title">周粒度设置</div>\r\n        <div class="content">\r\n            <input type="text" name="singleDateSetting" value="';
        $out+=$escape(item.defaultSelectedVal);
        $out+='"/>\r\n            <select>\r\n                <option value="D"';
        if(item.defaultSelectedUnit == 'D'){
        $out+=' selected';
        }
        $out+='>日</option>\r\n                <option value="W"';
        if(item.defaultSelectedUnit == 'W'){
        $out+=' selected';
        }
        $out+='>周</option>\r\n            </select>\r\n        </div>\r\n    </div>\r\n    ';
        }
        $out+='\r\n\r\n    ';
        if(item.type == 'M'){
        $out+='\r\n    <div class="item j-item" data-type="M">\r\n        <div class="title">月粒度设置</div>\r\n        <div class="content">\r\n            <input type="text" name="singleDateSetting" value="';
        $out+=$escape(item.defaultSelectedVal);
        $out+='"/>\r\n            <select>\r\n                <option value="D"';
        if(item.defaultSelectedUnit == 'D'){
        $out+=' selected';
        }
        $out+='>日</option>\r\n                <option value="W"';
        if(item.defaultSelectedUnit == 'W'){
        $out+=' selected';
        }
        $out+='>周</option>\r\n                <option value="M"';
        if(item.defaultSelectedUnit == 'M'){
        $out+=' selected';
        }
        $out+='>月</option>\r\n            </select>\r\n        </div>\r\n    </div>\r\n    ';
        }
        $out+='\r\n\r\n    ';
        if(item.type == 'Q'){
        $out+='\r\n    <div class="item j-item" data-type="Q">\r\n        <div class="title">季粒度设置</div>\r\n        <div class="content">\r\n            <input type="text" name="singleDateSetting" value="';
        $out+=$escape(item.defaultSelectedVal);
        $out+='"/>\r\n            <select>\r\n                <option value="D"';
        if(item.defaultSelectedUnit == 'D'){
        $out+=' selected';
        }
        $out+='>日</option>\r\n                <option value="W"';
        if(item.defaultSelectedUnit == 'W'){
        $out+=' selected';
        }
        $out+='>周</option>\r\n                <option value="M"';
        if(item.defaultSelectedUnit == 'M'){
        $out+=' selected';
        }
        $out+='>月</option>\r\n                <option value="Q"';
        if(item.defaultSelectedUnit == 'Q'){
        $out+=' selected';
        }
        $out+='>季</option>\r\n            </select>\r\n        </div>\r\n    </div>\r\n    ';
        }
        $out+='\r\n\r\n    ';
        if(item.type == 'Y'){
        $out+='\r\n    <div class="item j-item" data-type="Y">\r\n        <div class="title">季粒度设置</div>\r\n        <div class="content">\r\n            <input type="text" name="singleDateSetting" value="';
        $out+=$escape(item.defaultSelectedVal);
        $out+='"/>\r\n            <select>\r\n                <option value="D"';
        if(item.defaultSelectedUnit == 'D'){
        $out+=' selected';
        }
        $out+='>日</option>\r\n                <option value="W"';
        if(item.defaultSelectedUnit == 'W'){
        $out+=' selected';
        }
        $out+='>周</option>\r\n                <option value="M"';
        if(item.defaultSelectedUnit == 'M'){
        $out+=' selected';
        }
        $out+='>月</option>\r\n                <option value="Q"';
        if(item.defaultSelectedUnit == 'Q'){
        $out+=' selected';
        }
        $out+='>季</option>\r\n                <option value="Y"';
        if(item.defaultSelectedUnit == 'Y'){
        $out+=' selected';
        }
        $out+='>年</option>\r\n            </select>\r\n        </div>\r\n    </div>\r\n    ';
        }
        $out+='\r\n    ';
        });
        $out+='\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});