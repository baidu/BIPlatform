/**
 * @file: 报表首页
 * @author: weiboxue(wbx_901118@sina.com)
 * @date: 2014-11-20
 */
(function () {
    // dom元素容器
    var dom;
    // 提示信息
    var textcompany = '请填写所在部门名称';
    var textuserlimit = '用户名只能为英文字母及数字，请重新输入';
    var emaillimit = '邮箱格式不对请重新输入';
    var textemail = '请填写您的邮箱';
    var textrepass = '请确认密码';
    var textpass = '请输入密码';
    var textusename = '请输入用户名';
    var textdoublepass = '两次密码输入不一致，请重新输入';
    var errorsign = '用户名或密码输入错误';
    var textValidateCode = '请输入验证码';
    // 入口
    $(function () {
        dom = {
            register_sign: $('#home-button'),
            register_top: $('.register-top'),
            sign_top: $('.sign-top'),
            register_infor: $('.register-infor'),
            register_title: $('.register-title'),
            register: $('#register'),
            sign: $('#sign'),
            sign_usename: $('#sign-usename'),
            sign_pass: $('#sign-pass'),
            sign_validateCode: $('#sign-validateCode'),
            register_usename: $('#register-usename'),
            register_pass: $('#register-pass'),
            register_repass: $('#register-repass'),
            register_company: $('#register-company'),
            register_email: $('#register-email'),
            register_validateCode: $('#register-validateCode'),
            home_title: $('.home-title'),
            home_content: $('.home-content'),
            home_pic: $('.home-pic'),
            home_register_title: $('.home-register-title'),
            home_register_line: $('.home-register-line'),
            home_sign_title: $('.home-sign-title'),
            home_sign_line: $('.home-sign-line'),
            servicetype: $('#servicetype'),
            sign_enter: $('#sign-enter'),
            register_enter: $('#register-enter'),
            prompt: $('.prompt'),
            body: $('body'),
            validate_code: $('.validate-code')
        };
        bindEvents();
    });
    /**
     * 页面事件绑定
     */
    function bindEvents() {
        // 登录注册的事件
        fnRegisterSign();
        // 输入框内容清空恢复事件
        inputText();
        // 关闭登录和注册框
        closeSignReg();
        // 登录事件
        signIn();
        // 注册事件
        registerIn();
        // 验证码点击刷新
        clickRefreshValidateCode();
    }
    /**
     * 验证码点击刷新
     */
    var clickRefreshValidateCode = function () {
        var $validate = dom.validate_code;
        $validate.click(function () {
            var src = $(this).attr('src');
            $(this).attr('src', src + '?' + Math.random());
        });
    };
    /**
     * 验证码刷新
     */
    var refreshValidateCode = function (id) {
        var $validate = dom.validate_code;
        $validate.each(function () {
            if ($(this).attr('id') == id) {
                var src = $(this).attr('src');
                $(this).attr('src', src + '?' + Math.random());
            }
        });
    };
    /**
     * 注册按钮以及回车触发事件函数
     */
    var registerIn = function () {
        dom.register.click(function () {
            reisterJudge();
        });
        dom.register_enter.keydown(function (event) {
            if (event.keyCode == 13) {
                reisterJudge();
            }
        })
    };
    /**
     * 注册判定函数
     */
    var reisterJudge = function () {

        var $company = dom.register_company;
        var $email = dom.register_email;
        var $repass = dom.register_repass;
        var $pass = dom.register_pass;
        var $usename = dom.register_usename;
        var $servicetype = dom.servicetype;
        var $validateCode = dom.register_validateCode;
        if ($company.val() == '') {
            $company.next('div').html(textcompany);
        }
        if ($email.val() == '') {
            $email.next('div').html(textemail);
        }
        else if (!(/^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/).test($email.val())) {
            $email.next('div').html(emaillimit);
        }
        if ($repass.val() == '') {
            $repass.next('div').html(textrepass);
        }
        if ($pass.val() == '') {
            $pass.next('div').html(textpass);
        }
        if ($usename.val() == '') {
            $usename.next('div').html(textusename);
        }
        else if ((/[\u4e00-\u9fa5]+/).test($usename.val())){
            $usename.next('div').html(textuserlimit);
        }
        else if ((/[^0-9a-zA-Z]/g).test($usename.val())){
            $usename.next('div').html(textuserlimit);
        }
        if ($validateCode.val() == '') {
            $validateCode.parent().next('div').html(textValidateCode);
        }
        if (
            $pass.val() != ''
            && $repass.val() != ''
            && $usename.val() != ''
            && $email.val() != ''
            && $company.val() != ''
            && $validateCode.val() != ''
            && !((/[\u4e00-\u9fa5]+/).test($usename.val()))
            && !((/[\u4e00-\u9fa5]+/).test($email.val()))
            && !((/[^0-9a-zA-Z]/g).test($usename.val()))
            && (/^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/).test($email.val())
            ) {
            if ($pass.val() == $repass.val()) {
                $.ajax({
                    //客户端向服务器发送请求时采取的方式
                    type : "post",
                    cache : false,
                    //服务器返回的数据类型，可选 XML, Json, jsonp, script, html, text。
                    dataType : 'Json',
                    //指明客户端要向哪个页面里面的哪个方法发送请求
                    url : "/silkroad/register",
                    data : {
                        name : $usename.val(),
                        pwd : $pass.val(),
                        department: $company.val(),
                        email: $email.val(),
                        serviceType: $servicetype.val(),
                        validateCode: $validateCode.val()
                    },
                    //客户端调用服务器端方法成功后执行的回调函数
                    success : function(msg) {
                        if (msg.status === 0) {
                            alert('注册成功,请注意查收邮件');
                        }
                        else {
                            alert('注册失败：' + msg.statusInfo);
                            // 注册失败验证码刷新
                            refreshValidateCode('register-code');
                        }
                        //$.get('www.baidu.com');
                        //$("#resText").html(msg);
                        /*
                         if (result.d=="success") {
                         alert("登陆成功");
                         } else {
                         alert("登录失败");
                         }*/
                    }
                });
            }
            else {
                $pass.val('');
                $repass.val('');
                $pass.attr('placeholder', textdoublepass);
                $repass.attr('placeholder', textdoublepass);
            }
        }
    };
    /**
     * 登录按钮以及回车触发事件函数
     */
    var signIn = function () {
        dom.sign.click(function () {
            signJudge();
        });
        dom.sign_enter.keydown(function (event) {
            if (event.keyCode == 13) {
                signJudge();
            }
        })
    };
    /**
     * 登录判定函数
     */
    var signJudge  = function () {

        var $pass = dom.sign_pass;
        var $usename = dom.sign_usename;
        var $signvalidateCode = dom.sign_validateCode;

        if ($usename.val() == '') {
            $usename.next('div').html(textusename);
        }
        else if ((/[\u4e00-\u9fa5]+/).test($usename.val())){
            $usename.next('div').html(textuserlimit);
        }
        else if ((/[^0-9a-zA-Z]/g).test($usename.val())){
            $usename.next('div').html(textuserlimit);
        }
        if ($pass.val() == '') {
            $pass.next('div').html(textpass);
            return;
        }
        if ($pass.val() == '') {
            $pass.next('div').html(textpass);
            return;
        }
        if ($signvalidateCode.val() == '') {
            $signvalidateCode.parent().next('div').html(textValidateCode);
            return;
        }
        if (
            $pass.val() != ''
            && $usename.val() != ''
            && !((/[\u4e00-\u9fa5]+/).test($usename.val()))
            && !((/[^0-9a-zA-Z]/g).test($usename.val()))
        ) {
            $.ajax({
                //客户端向服务器发送请求时采取的方式
                type : "post",
                cache : false,
                //服务器返回的数据类型，可选 XML, Json, jsonp, script, html, text。
                dataType : 'Json',
                //指明客户端要向哪个页面里面的哪个方法发送请求
                url : "/silkroad/login",
                data : {
                    name : $usename.val(),
                    pwd : $pass.val(),
                    validateCode: $signvalidateCode.val()
                },
                //客户端调用服务器端方法成功后执行的回调函数
                success : function(msg) {
                    var sign = msg.status;
                    if (sign != 1) {
                        // window.location = "/silkroad/index.html";
                        msg.data ? (window.location = msg.data) : (window.location = "/silkroad/index.html");
                    }
                    else {
//                        $pass.next('div').html(errorsign);
//                        $usename.next('div').html(errorsign);
                        $signvalidateCode.parent().next('div').html(msg.statusInfo);
                        // 登录失败验证码刷新
                        refreshValidateCode('sign-code');
                    }
                }
            });
        }

    };
    /**
     * 关闭登录和注册框
     */
    var closeSignReg = function () {
        dom.register_title.find('div').click(function () {
            fnAnimateReturn();
        });
        dom.body.keydown(function (event) {
            if(event.keyCode == 27) {
                fnAnimateReturn();
            }
        })
    };

    /**
     * 输入框获取焦点清空
     */
    var inputText = function () {
        dom.register_infor.focus(function () {
            $(this).next('div').html('');
        });
    };
    /**
     * 登录，注册按钮事件
     */
    var fnRegisterSign = function () {
        // 登录，注册弹出框
        dom.register_sign.find('div').click(function () {
            var $retitle = dom.home_register_title;
            var $sititle = dom.home_sign_title;
            var $reline = dom.home_register_line;
            var $siline = dom.home_sign_line;
            var $retop = dom.register_top;
            var $sitop = dom.sign_top;
            $retop.hide();
            $sitop.hide();
            if ($(this).attr('class') == 'registration') {
                fnAnimateHide('0px', '575px', '260px', 300);
                fnAnimateShow('200px', '407px', $retitle, $reline, $retop);
            }
            else if ($(this).attr('class') == 'experience') {
                fnAnimateHide('500px', '375px', '460px', 300);
                fnAnimateShow('-50px', '365px', $sititle, $siline, $sitop)
            }
        });
        /**
         * 切换动画函数(主页元素)
         *
         * @param {string} letit 主页题目左定位距离
         * @param {string} lecon 下方文字条左定位距离
         * @param {string} lesig 登录注册按钮左定位距离
         * @param {number} time 下划线对象
         * @public
         */
        var fnAnimateHide = function (letit, lecon, lesig, time) {
            var $hotitle = dom.home_title;
            var $hocontent = dom.home_content;
            var $hosign = dom.register_sign;
            $hotitle.animate({'left': letit, 'opacity': '0'}, time, function () {
                $hotitle.hide();
            });
            $hocontent.animate({'left': lecon, 'opacity': '0'}, time, function () {
                $hocontent.hide();
            });
            $hosign.animate({'left': lesig, 'opacity': '0'}, time, function () {
                $hosign.hide();
            });
        };
        /**
         * 切换动画函数(登录注册框元素)
         *
         * @param {string} lepic 报表图片左定距离
         * @param {string} widthline 下划线长度
         * @param {object} title 登录注册标题
         * @param {object} line 下划线对象
         * @param {object} box 登录注册框体
         * @public
         */
        var fnAnimateShow = function (lepic, widthline, title, line, box) {
            var $hopic = dom.home_pic;
            $hopic.animate({'left': lepic, 'opacity': '0'}, 300, function () {
                $hopic.hide();
                title.fadeIn(200);
                line.animate({'width': widthline}, 300);
                box.show();
            });
        };
    };
    /**
     * 切换会主页面动画函数
     */
    var fnAnimateReturn = function () {
        var $hotitle = dom.home_title;
        var $hocontent = dom.home_content;
        var $resign = dom.register_sign;
        var $retitle = dom.home_register_title;
        var $hopic = dom.home_pic;
        var $reline = dom.home_register_line;
        var $retop = dom.register_top;
        var $sitop = dom.sign_top;
        var $sititle = dom.home_sign_title;
        var $siline = dom.home_sign_line;
        $hotitle.show();
        $hocontent.show();
        $resign.show();
        $hopic.show();
        $retop.hide();
        $sitop.hide();
        $retitle.fadeOut(200);
        $reline.animate({'width': '0px'},300);
        $sititle.fadeOut(200);
        $siline.animate({'width': '0px'},300, function () {
            $hotitle.animate({'left': '300px', 'opacity': '1'}, 300);
            $hocontent.animate({'left': '475px', 'opacity': '1'}, 300);
            $resign.animate({'left': '360px', 'opacity': '1'}, 300);
            $hopic.animate({'left': '100px', 'opacity': '1'}, 200);
        });
        // 关闭登录注册框时清空信息
        dom.register_infor.val('');
        dom.prompt.html('');
    };
})();