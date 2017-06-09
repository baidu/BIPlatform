/**
 * @file: 重定向url，用于解决地址重复的问题
 * @author: 赵晓强(longze_xq@163.com)
 * date: 2014-08-21
 */
define(function () {

    // 闭包变量

    // map中添加需要做转发的配置信息
    // from定义需要转的路径
    // type定义需要转的methodType
    // to定义要转到的地址
    var map = [
        {
            from: 'reports',
            type: 'POST',
            to: 'reports-post'
        },
        {}
    ];

    /**
     * 处理请求路径
     * @param {Object} option ajax参数对象
     * @public
     */
    function process(option) {
        for (var i = 0, len = map.length; i < len; i++) {
            if (option.url === map[i].from && option.type === map[i].type) {
                option.url = map[i].to;
            }
        }
    }

    return process;
});