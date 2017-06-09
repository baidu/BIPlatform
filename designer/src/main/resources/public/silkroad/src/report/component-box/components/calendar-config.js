/**
 * @file form（component）中对应calendar（vui）的配置信息
 * @author 赵晓强, lizhantong(lztlovely@126.com)
 * @date 2014-9-10
 */
define(
    [
        'constant',
        'report/component-box/components/calendar-vm-template'
    ],
    function (
        Constant,
        CalendarVmTemplate
    ) {
        // 日历id后缀
        var calendarIdSuffix = Constant.COMPONENT_ID_SUFFIX.CALENDAR;

        // 日历 实例 描述信息（从report-ui里面获取）
        var entityDescription = {
            clzType: 'VUI',
            dataSetOpt: {
                forbidEmpty: false,
                disableCancelBtn: false,
                timeTypeList: [],
                timeTypeOpt: {}
            },
            name: 'dim_time^_^the_date', // name在report-ui里面会根据dateKey里面的内容改变
            dateKey: {},
            clzKey: 'X_CALENDAR'
        };

        var entityDescriptionRangeCalendar = {
            clzType: 'VUI',
            name: 'dim_time^_^the_date', // name在report-ui里面会根据dateKey里面的内容改变
            dateKey: {},
            dataSetOpt: {
                rangeTimeTypeOpt: {
                    startDateOpt: 0,
                    endDateOpt: 0
                }
            },
            clzKey: 'RANGE_CALENDAR'
        };

        var rangeConfig = {
            start: 0,
            end: 0
        };

        // 那些个外在的配置项
        var timeTypeConfig = {
            timeTypeList: {
                D: {
                    value: 'D',
                    text: '日'
                },
                W: {
                    value: 'W',
                    text: '周'
                },
                M: {
                    value: 'M',
                    text: '月'
                },
                Q: {
                    value: 'Q',
                    text: '季'
                }
            },
            timeTypeOpt: {
                D: {
                    selMode: 'SINGLE',
                    // 默认时间
                    date: [
                        '-1D',
                        '-1D'
                    ],
                    // 事件范围
                    range: [
                        '2011-01-01',
                        '0D'
                    ],
                    selModeList: [
                        {
                            text: '单选',
                            value: 'SINGLE',
                            prompt: '单项选择'
                        }
                    ]
                },
                W: {
                    selMode: 'RANGE',
                    date: [
                        '-1W',
                        '-1W'
                    ],
                    range: [
                        '2011-01-01',
                        '0W'
                    ],
                    selModeList: [
                        {
                            text: '单选',
                            value: 'SINGLE',
                            prompt: '单项选择'
                        }
//                        {
//                            'text': '范围多选',
//                            'value': 'RANGE',
//                            'prompt': '范围选择，点击一下选择开始值，再点击一下选择结束值'
//                        }
                    ]
                },
                M: {
                    selMode: 'SINGLE',
                    date: [
                        '-1M',
                        '-2M'
                    ],
                    range: [
                        '2011-01-01',
                        '0M'
                    ],
                    selModeList: [
                        {
                            text: '单选',
                            value: 'SINGLE',
                            prompt: '单项选择'
                        }
//                        {
//                            'text': '范围多选',
//                            'value': 'RANGE',
//                            'prompt': '范围选择，点击一下选择开始值，再点击一下选择结束值'
//                        }
                    ]
                },
                Q: {
                    selMode: 'SINGLE',
                    date: [
                        '-1Q',
                        '-2Q'
                    ],
                    range: [
                        '2011-01-01',
                        '0D'
                    ],
                    selModeList: [
                        {
                            text: '单选',
                            value: 'SINGLE',
                            prompt: '单项选择'
                        }
                    ]
                }
            }
        };

        /**
         * 处理渲染数据（json的数据）
         *
         * @param {Object} dynamicData 动态数据
         * return {Object} 处理之后的数据
         * private
         */
        function processRenderData(dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, {}, entityDescription);
            data.id = id + calendarIdSuffix;

            return data;
        }

        /**
         * 转换日历控件的配置信息（暂时简单做）
         *
         * @param {Array} data  前端整理后的数据
         * return {Object} 转换后的数据，包括两部分：timeTypeList 与 timeTypeOpt
         */
        function switchConfig(data) {
            var resTimeType = [];
            var resTimeTypeOpt = {};
            var rangeTimeTypeOpt = {};
            var timeTypeList = timeTypeConfig.timeTypeList;
            var timeTypeOpt = timeTypeConfig.timeTypeOpt;

            for (var i = 0, len = data.length; i < len; i++) {
                var type = data[i].type;
                // 匹配timeTypeList
                resTimeType.push(timeTypeList[type]);
                // 匹配timeTypeOpt
                var opt = timeTypeOpt[type];
                opt.date = data[i].date;
                // 设置date range的情况
                if (data[i].startDateOpt !== undefined && data[i].endDateOpt !== undefined){
                    rangeTimeTypeOpt.startDateOpt = data[i].startDateOpt;
                    rangeTimeTypeOpt.endDateOpt = data[i].endDateOpt;
                }
                resTimeTypeOpt[type] = $.extend(true, {}, opt);
            }

            return {
                timeTypeList: resTimeType,
                timeTypeOpt: resTimeTypeOpt,
                rangeTimeTypeOpt: rangeTimeTypeOpt
            }
        }

        /**
         * 逆转换日历控件的配置信息（转换为可以还原展示的数据格式）
         *
         * @param {Object} timeTypeOpt 待转换数据，主要是
         * @private
         * @return {Array} data  整理后的数据
         */
        function deSwitchConfig(timeTypeOpt) {
            var data = [];
            var fromItem;
            var date;
            for (var name in timeTypeOpt) {
                var toItem = {};

                fromItem = timeTypeOpt[name];

                // 类型
                toItem.type = name;
                // 临时处理，如果添加功能需要升级此处代码
                date = fromItem.date[0];
                // 默认时间的偏差值
                toItem.defaultSelectedVal = date.substr(0, date.length-1);
                // 默认时间的偏差单位
                toItem.defaultSelectedUnit = date.substr(date.length-1, 1);

                data.push(toItem);
            }

            return data;
        }

        /**
         * 将后台给的单词转换成为前台需要的字母
         *
         * @param {string} word 后台给的单词
         * @private
         * @return {string} letter 前台需要的单词
         */
        function switchLetter (word) {
            var letter;

            switch (word) {
                case 'ownertable_TimeDay':
                    letter = 'D';
                    break ;
                case 'ownertable_TimeWeekly':
                    letter = 'W';
                    break ;
                case 'ownertable_TimeMonth':
                    letter = 'M';
                    break ;
                case 'ownertable_TimeQuarter':
                    letter = 'Q';
                    break ;
                case 'ownertable_TimeYear':
                    letter = 'Y';
                    break ;
            }

            return letter;
        }

        return {
            type: 'TIME_COMP',
            caption: '日历',
            iconClass: 'calendar',
            defaultWidth: 300,
            defaultHeight: 33,
            vm: {
                render: function (data) {
                    return CalendarVmTemplate.render({
                        id: data.rootId + data.serverData.id + calendarIdSuffix
                    });
                }
            },
            entityDescription: entityDescription,
            entityDescriptionRangeCalendar: entityDescriptionRangeCalendar,
            processRenderData: processRenderData,
            rangeConfig: rangeConfig,
            switchConfig: switchConfig,
            deSwitchConfig: deSwitchConfig,
            timeTypeConfig: timeTypeConfig,
            switchLetter: switchLetter
        };
    });