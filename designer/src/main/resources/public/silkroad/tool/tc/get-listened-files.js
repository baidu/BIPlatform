/**
 * 获取需要监听的文件列表
 */
var fs = require('fs');
var config = require('./config');

/**
 * 读取指定文件夹下的全部文件，可通过正则进行过滤，返回文件路径数组
 * @param {String} root 指定文件夹路径
 * [@param] reg 对文件的过滤正则表达式,可选参数
 *
 * 注：还可变形用于文件路径是否符合正则规则，
 * 路径可以是文件夹，也可以是文件，对不存在的路径也做了容错处理*/
function readAllFile(root, reg) {
    var resultArr = [];
    var thisFn = arguments.callee;

    if (fs.existsSync(root)) {//文件或文件夹存在
        var stat = fs.lstatSync(root); // 对于不存在的文件或文件夹，此函数会报错

        if (stat.isDirectory()) {// 文件夹
            var files = fs.readdirSync(root);
            files.forEach(function (file) {
                var t = thisFn(root + '/' + file, reg);
                resultArr = resultArr.concat(t);
            });
        }
        else {
            if (reg !== undefined) {

                if (typeof reg.test == 'function' && reg.test(root)) {
                    resultArr.push(root);
                }
            }
            else {
                resultArr.push(root);
            }
        }
    }

    return resultArr;
}

module.exports = readAllFile(config.rootPath, config.templateFileReg);