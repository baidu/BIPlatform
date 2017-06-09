/**
 * @file:    jquery常用方法扩展
 * @author:  lzt(lztlovely@126.com)
 * @date:    2014/07/14
 */

//------------------------------------------
// 包装器扩展区
//------------------------------------------

/**
 * 设置光标位置
 *
 * @param {number} position 光标位置
 */
$.fn.setCursorPosition = function(position){
    if(this.length === 0)  {
        return this;
    }

    return $(this).setSelection(position, position);
};

/**
 * 设置光选中内容
 * 如果是设定光标位置，那么selStart为光标位置，selEnd为1即可
 * 如果需要选中内容，那么selStart为选中内容开始位置，selEnd为选中内容长度
 * 真正选中内容的长度为：selEnd-1
 *
 * @param {number} selStart 选中内容开始位置
 * @param {number} selEnd 选中内容结束位置
 */
$.fn.setSelection = function(selStart, selEnd) {
    var input;
    if(this.length == 0) {
        return this;
    }
    input = this[0];

    if (input.createTextRange) {
        var range = input.createTextRange();
        range.collapse(true);
        range.moveEnd('character', selStart);
        range.moveStart('character', selEnd);
        range.select();
    } else if (input.setSelectionRange) {
        input.focus();
        input.setSelectionRange(selStart, selEnd);
    }

    return this;
};

/**
 * 得到焦点
 *
 * @param {number} position 光标位置
 */
$.fn.diFocus = function(position) {
    if (position && typeof position === 'number') {
        this.setCursorPosition(position);
    } else {
        this.setCursorPosition(this.val().length);
    }
    return this;
};

//------------------------------------------
// 实用工具函数扩展区
//------------------------------------------

/**
 * 获取光标位置
 *
 * @param {HTMLElement} el dom对象
 */
$.getCursorPosition = function (el) {
    var curIndex = -1;
    var range;

    if (el.setSelectionRange){    // W3C
        curIndex = el.selectionStart;
    } else {    // IE
        range = document.selection.createRange();
        range.moveStart("character", -el.value.length);
        curIndex = range.text.length;
    }
    return curIndex;
};

/**
 * 当前元素是否在数组中
 *
 * @param {HTMLElement} el dom对象
 */
$.isInArray = function (item, array) {
    var flag = false;
    for (var i = 0; i < array.length; i ++) {
        if (item === array[i]) {
            flag = true;
        }
    }
    return flag;
};

/**
 * 对象是否为空属性的对象
 *
 * @param {Object} obj 对象
 */
$.isObjectEmpty = function (obj) {
    var flag = true;
    for (var key in obj) {
       if (obj.hasOwnProperty(key)) {
           flag = false;
       }
    }
    return flag;
};

/**
 * 对目标字符串进行html编码 (@see tangram)
 * 编码字符有5个：& < > " '
 *
 * @public
 * @param {string} source 目标字符串
 * @returns {string} html编码后的字符串
 */
$.encodeHTML = function(source) {
    return String(source)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
};

// 获取到含有当前id的组件实例
// TODO:写注释
$.getTargetElement = function (id, entityArray) {
    var target;
    for (var i = 0, iLen = entityArray.length; i < iLen; i ++) {
        if (entityArray[i].clzType === 'COMPONENT' && entityArray[i].compId === id) {
            target = entityArray[i];
            break;
        }
    }
    return target;
};

//获取到含有当前id的组件实例
//TODO:写注释
$.getVuiTargetElement = function (id, entityArray) {
 var target;
 for (var i = 0, iLen = entityArray.length; i < iLen; i ++) {
     if (entityArray[i].clzType === 'VUI' && entityArray[i].id === id) {
         target = entityArray[i];
         break;
     }
 }
 return target;
};

// 获取到含有当前id的组件实例的clzType
$.getTargetElementClzType = function (id, entityArray) {
    return $.getTargetElement(id, entityArray).clzType;
};

// 判断当前实例中是否已有事件关联:如果返回-1，说明没有关联
// TODO:写注释
$.hasRelation = function (id, entity) {
    var result = -1;
    if (entity.interactions) {
        interactionsLoop:
        for (var i = 0, iLen = entity.interactions.length; i < iLen; i ++) {
            var interaction = entity.interactions[i];
            if (interaction.event) {
                if (entity.interactions[i].event.rid === id) {
                    result = i;
                    break;
                }
            }
            else if (interaction.events) {
                for (var j = 0, jLen = interaction.events.length; j < jLen; j ++) {
                    if (interaction.events[j].rid === id) {
                        result = i;
                        break interactionsLoop;
                    }
                }
            }
        }
    }
    return result;
};

// 判断当前实例中是否已有事件关联
// TODO:写注释
$.getEntityInteractionsId = function (entity) {
    var result = [];
    if (entity.interactions) {
        for (var i = 0, iLen = entity.interactions.length; i < iLen; i ++) {
            var interaction = entity.interactions[i];
            if (interaction.event) {
                result.push(entity.interactions[i].event.rid);
            }
            else if (interaction.events) {
                for (var j = 0, jLen = interaction.events.length; j < jLen; j ++) {
                    result.push(interaction.events[j].rid);
                }
            }
        }
    }
    return $.uniqueArray(result);
};

$.uniqueArray = function (arry) {
    var target = [];
    var records = {};
    for (var i = 0; i < arry.length; i++) {
        if (!records[arry[i]]) {
            records[arry[i]] = true;
            target.push(arry[i]);
        }
    }
    return target;
};

/**
 * 在entitys中是否已经存在某一个entity
 *
 * @param {string} clzKey 组件实例的clzKey
 * @param {Array} entitys 组件实例数组
 */
$.isHaveEntity = function (clzKey, entitys) {
    var result = false;
    for (var i = 0; i < entitys.length; i++) {
        if (entitys[i].clzKey === clzKey) {
            result = true;
        }
    }
    return result;
};

/**
 * 是否是正整数
 *
 * @param {string} val 待验证字符串
 *
 * @return {boolean}
 */
$.isPositiveInt = function (val) {
    var result = false;
    var numberMax = 2147483647;
    val = Number(val);

    if (val > 0 && val <= numberMax) {
        result = true;
    }
    return result;
};


