/**
 * Created by v_zhaoxiaoqiang on 2014/7/16.
 * 创建 View 与 Model
 * 第一个参数为路径，第二个参数为模块名称
 * 使用示例：node c data-sources\list main
 */
(function () {
    var fs = require('fs');
    var arg = process.argv.splice(2);
    var basePath = '../src/';

    if (arg[0] !== undefined && arg[1] !== undefined) {
        var userInputPath = arg[0] + '/' + arg[1];
        var wrightPath = basePath + arg[0];
        var fileName = arg[1];
    }
    else {
        console.log('参数未完全指定,第一个为路径,第二个为模块名称如"report-list"');
        return;
    }

    var date = new Date();
    var dateStr = date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate();
    if (fs.existsSync(wrightPath)) {

        var viewFilePath = wrightPath + '/' + fileName + '-view.js';
        var modelFilePath = wrightPath + '/' + fileName + '-model.js';
        // modelTemplateContent
        var mc = fs.readFileSync('./model-template.txt', 'utf-8') + '';
        // viewTemplateContent
        var vc = fs.readFileSync('./view-template.txt', 'utf-8') + '';

        // 一些内容的自动化
        mc = mc.replace('{{date}}', dateStr);
        vc = vc.replace('{{date}}', dateStr);

        vc = vc.replace(/{{ModelName}}/g, toCamel(fileName, true));
        vc = vc.replace(/{{ModelName2}}/g, toCamel(fileName));
        vc = vc.replace(/{{ModelUrl}}/g, userInputPath + '-model');
        vc = vc.replace(/{{templateUrl}}/g, userInputPath + '-template');

        // 添加model文件
        addFile(modelFilePath, mc);
        // 添加view文件
        addFile(viewFilePath, vc);
    }
    else {
        console.log('不存在路径：' + wrightPath);
        return;
    }

    /*------------工具函数------------*/
    /**
     * 添加文件
     * @param {String} 文件路径
     * @param {String} 文件内容
     * */
    function addFile(path, content) {
        if (!fs.existsSync(path)) {
            fs.appendFile(path, content, 'utf8', function (err) {
                if (err) {
                    console.log(path + '添加失败');
                    console.log(err);
                } else {
                    console.log(path + '添加成功');
                }
            });
        }
    }

    /**
     * 中划线转驼峰
     * @param {String} 待转换的字符串
     * @param {Bool} 是否是类（是否将第一个字母转换成大写）
     *
     * @return {String} 转换后的字符串*/
    function toCamel(str, isClass) {
        str = str.replace(/\-(\w)/g, function (all, letter) {
            return letter.toUpperCase();
        });

        if(isClass === true){
            str = str.replace(/^(\w)/g, function (all, letter) {
                return letter.toUpperCase();
            });
        }

        return str;
    }
})();