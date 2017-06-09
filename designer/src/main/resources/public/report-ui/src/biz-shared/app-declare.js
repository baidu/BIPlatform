/**
 * project declaration
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    项目起始文件，全局声明
 * @author:  sushuang(sushuang)
 * @depend:  xui.XProject
 */

// 如果打包时使用(function() { ... })()包裹住所有代码，
// 则以下声明的变量在闭包中；
// 否则以下声明的变量暴露到全局。
 
// DI名空间基础
xui.XProject.setNamespaceBase(
    window.__$DI__NS$__ = window.__$DI__NS$__ || {}
);

// 声明名空间用方法
var $namespace = xui.XProject.namespace;

// 注册依赖连接用方法
var $link = xui.XProject.link;

// 注册延迟初始化用方法
var $end = xui.XProject.end;

// 得到名空间根基
var $getNamespaceBase = xui.XProject.getNamespaceBase;

// DI根名空间
var di = $namespace('di');

// FIXME
// 暂时用这种方法注册进去
$getNamespaceBase().xui = xui;
$getNamespaceBase().xutil = xutil;

xutil.object.PATH_DEFAULT_CONTEXT = $getNamespaceBase();