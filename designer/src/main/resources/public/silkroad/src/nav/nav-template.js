/**
 * @file: 导航模块 Html Template
 * @author: lizhantong(lztlovely@126.com)
 */
define(['template'], function (template) {

    var html = [
        '<ul class="nav-main j-nav-main">',
            '{{each menus as $menu}}',
            '<li class="nav-menu{{if $menu.id === currentMenu}} nav-menu-focus{{/if}}" id="{{$menu.id}}">',
                '<span class="nav-menu-span">{{$menu.name}}</span>',
            '</li>',
            '{{/each}}',
        '</ul>'
    ].join('');
        
    return {
        render: function (data) {
            var func = template.compile(html);
            var result = func(data);
            return result;
        }
    };
});