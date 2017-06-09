# 报表配置端2.0版前端概要设计方案

## 目录规范

    ${root}/
        asset/ src中的文件压缩后的文件
        dep/ 依赖的js库，不做压缩处理，线上线下用法相同
        doc/ 文档
        src/ 资源文件
            common/ 用于存放自己开发的组件与一些通用的功能模块
            css/ 存放样式与装饰图片
                biz/ 业务模块样式
                    all.css 此文件夹中的其他文件的@import引用集合
                    ...
                component/ 组件样式
                    all.css 此文件夹中的其他文件的@import引用集合
                    ... 各组件的样式
                img/ 装饰图片
                all.css base、skin、biz\all等css文件的@import引用集合
                base.css 基础样式
                skin-blue.css 蓝色皮肤
                ... 其他皮肤样式
            data-sources/ 业务模块--数据源
            ... 其他的业务模块
            enter.js 单页面应用入口
        test/ 测试与模拟数据，还包括demo
            mock-data/ 模拟数据
            demo/ 组件等的示例
        tool/ 前端压缩与一些辅助开发工具

## 资源配置

压缩后dep原样保存，src中的资源压缩到asset中。
	
	require.config({
        baseUrl: 'src/',  // 线上版用asset压缩版
        paths: {
            underscore: '../dep/underscore-1.6.0.min',
            jquery: '../dep/jquery-1.11.1.min',
            backbone: '../dep/backbone-1.1.2.min',
            template: '../dep/template'
        },
        shim: {
            'backbone': {
                deps: [
                    'underscore',
                    'jquery'
                ]
            }
        }
    });


## 框架与库的使用 ##

`requirejs`作为模块的加载与自动化管理器

`backbone`作为mvc的组织框架

`underscore`为backbone提供js底层的兼容

`jquery`为backbone提供dom操作支持

`arttemlate`提供模板定义与渲染的支持

`jqueryUI`弹框拖拽等UI组件的支持

## 模块间的分工与协作
model用于获取与保存数据以及数据的预处理

model代码示例：

	var MyModel = Backbone.Model.extend({
        // 集中事件绑定
        events:{
            'change:dataSources': 'getDataTable'
        },initialize: function(){

            // 更具业务逻辑来动态绑定事件
            this.bind('change:dataSources',function(){
                // do something
            });
        },
        // 数据的提交、获取与处理都可以在此处定义
        getDataTable: function(){
            this.set({dataTable:[]});
        }
    });
	
view代码示例：

    var MyView = Backbone.View.extend({
        // 集中事件绑定
        events:{
            'click .j-get-data-table': 'getDataTable'
        },
        render: function(data) {
            $(this.el).html('<div class="j-get-data-table">j-get-data-table</div>');
            return this; //方便链式调用
        },
        // 构造函数
        initialize: function(){
            
            // 通过监听触发的事件
            this.listenTo(this.model, 'change:dataTable',function(){
                // do something
            });
        },
        getDataTable: function(){
            console.log('view中的getDataTable！！');
            this.model.getDataTable();
        }
    });
	
实例化使用代码示例：

    var myView = new MyView({el: $('#test'), model: new MyModel()});
    myView.render();

## 模块间的交互

如果模块A需要被其他模块调用操作，那么将A挂在window.dataInsight下，以供其他模块操作。其挂载的时机建议在模块实例化之后。

关键代码示例：

模块挂载：

    var myView = new MyView({el: $('.j-my-view'), model: new MyModel()});
    myView.render();

    // 页面资源集中“叠罗汉”（因为collection对象嵌套很不方便，所以采用原生方式）
    window.dataInsight = window.dataInsight||{};
    window.dataInsight.myView = myView;
    
模块调用：
	
	// 需要深度克隆，否则不会触发change事件
    var dataTable = $.extend(true, {}, window.dataInsight.myView.model.get('dataTable'));
    dataTable.push(item); //其中item是经过处理的数据项
    window.dataInsight.myView.set({'dataTable':dataTable});

## 页面入口

将模块的入口独立出来，同时方便后续做路由支持。

	require(['enter'], function (main) {
        main.enter();
    });

## 接口规范与客户端服务器交互方式

接口采用restful风格；

不符合restful场景的接口归入到各模块的search下；

增删改查与search范例如下

添加 create → POST （当model没有ID时）

    model.save(
        this.attributes,
        {
            // 每一个都需要写了
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},                
            success: function () {
                console.log('model create ok');
            }
        }
    );

更新 update → PUT （当model有ID时）

    model.save(
            this.attributes,
            {
                // 每一个都需要写了
                beforeSend: function (jqXHR, se) {
                    se.url += '/' + that.model.get('id');                    
                },
                success: function () {
                    
                }
            }
    );
    
查找 read → GET

    model.fetch(
        {
            // 每一个都需要写了
            beforeSend: function (jqXHR, se) {
                se.url += '/' + that.model.get('id');
            },
            data: {
                name: 'jack'
            },
            success: function (model, data) {
            
            }
        }
    );
    
删除 delete → DELETE

    model.destroy(
        {
            beforeSend: function (jqXHR, se) {
                se.url += '/' + that.model.get('id');
            },
            success: function () {
            
            }
        }
    );