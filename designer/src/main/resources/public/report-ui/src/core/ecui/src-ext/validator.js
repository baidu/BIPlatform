/**
 * validator
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * path:    validator.js
 * desc:    验证器 
 * author:  cxl(chenxinle)
 * date:    2012/03/09
 */
(function () {

    var core = ecui,
        ui = core.ui,
        util = core.util,

        REGEXP = RegExp,
        triggerEvent = core.triggerEvent,

        VALIDATOR_RULES = {},
        VALIDATOR_RULES_ORDER = [];
        
    util.validator = {
        /**
         * 根据验证规则对数据进行验证
         * @public
         *
         * @param {Object}  value   待验证的数据
         * @param {Array}   rules   验证规则集合，按照数组顺序进行验证
         *
         * @return {Object} 验证结果 
         *      {Boolean}   state   验证结果
         *      {String}    name    验证失败时对应的验证规则名称
         *      {Object}    rule    验证失败时对应的验证要求
         */
        validate: function (value, rules) {
            var i = 0, item, res = true;

            while (res && rules[i]) {
                item = rules[i++];
                res = VALIDATOR_RULES[item.name].call(null, value, item.rule)
            }

            //构造验证结果对象
            if (!res) {
                res = {state: false, name: item.name, rule: item.rule};
            }
            else {
                res = {state: true};
            }
            return res;
        },

        /**
         * 增加验证器
         * @public
         *
         * @param {String}      ruleName    验证器规则的名称
         * @param {Function}    call        验证函数 接口形式如下
         *      @param {Object}     value   待验证的数据
         *      @param {Object}     rule    验证要求
         *      @return {Boolean}   验证是否通过
         */
        addRule: function (ruleName, call) {
            VALIDATOR_RULES[ruleName] = call;
            VALIDATOR_RULES_ORDER.push(ruleName);
        },

        /**
         * 收集验证信息
         * 按照系统默认的验证顺序收集验证规则
         * 用于在控件收集自生的验证规则
         *
         * @param {Object}  参数 key: 验证规则名, value:验证要求
         * @return {Array}  验证规则
         *      {String} name 验证名称
         *      {Object} rule 验证要求
         */
        collectRules: function (options) {
            var i, name, rules = [];

            for (i = 0; name = VALIDATOR_RULES_ORDER[i]; i++) {
                if (options.hasOwnProperty(name)) {
                    rules.push({name: name, rule: options[name]});
                }
            }

            return rules;
        }
    };

    /**
     * 添加非空验证器
     */
    util.validator.addRule('require', function (value) {
        if ('[object Array]' == Object.prototype.toString.call(value)) {
            return value.length !== 0;
        }
        else {
            value += '';
            return !!value;
        }
    });

    /**
     * 添加正则验证器
     */
    util.validator.addRule('patterMatch', function (value, rule) {
        var exp = new REGEXP(rule);

        return exp.test(value);
    });

    /**
     * 添加类型验证器
     * 支持URL, email格式验证
     */
    util.validator.addRule('typeMatch', function (value, rule) {
        var patter = {
            url: '^[^.。，]+(\\.[^.，。]+)+$',
            email: '^[_\\w-]+(\\.[_\\w-]+)*@([\\w-])+(\\.[\\w-]+)*((\\.[\\w]{2,})|(\\.[\\w]{2,}\\.[\\w]{2,}))$'
        };

        rule = patter[rule];
        if (rule && (new REGEXP(rule)).test(value)) {
            return true;
        }
        else {
            return false;
        }
    });


    /**
     * 最大字符长度验证器
     */
    util.validator.addRule('maxLength', function(value, rule) {
        return value.length <= rule;
    });

    /**
     * 最小字符长度验证器
     */
    util.validator.addRule('minLength', function(value, rule) {
        return value.length >= rule;
    });

    /**
     * 最大值验证器
     */
    util.validator.addRule('maxValue', function(value, rule) {
        return value <= rule;
    });

    /**
     * 最小值验证器
     */
    util.validator.addRule('minValue', function(value, rule) {
        return value >= rule;
    });

    /**
     * 扩展Input控件，添加验证方法
     */
    var UI_INPUT_CONTROL_CLASS = ui.InputControl.prototype;

    /**
     * override
     */
    UI_INPUT_CONTROL_CLASS.validate = function () {
        var res = true, valid;

        valid = util.validator.validate(this.getValue(), this._aValidateRules);
        res = valid.state;
        if (!res) {
            triggerEvent(this, 'invalid', null, [valid.name, valid.rule]);
        }
        return res;
    };

    /**
     * 设置验证规则
     * @public
     *
     * @param {String} name 验证规则名称
     * @param {Object} rule 验证要求
     */
    UI_INPUT_CONTROL_CLASS.setValidateRules = function (options) {
        this._aValidateRules = util.validator.collectRules(options);
    };
})();
