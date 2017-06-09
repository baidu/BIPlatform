var a = {
    "series": [{
        "data": [10, 20, 30, 40, 50, 50, 40, 30, 20],
        "name": "页面浏览量",
        "yAxisIndex": 0,
        "type": "bar",
        "barMaxWidth": 20
    }],
    "tooltip": {"trigger": "axis", "textStyle": {"fontFamily": "微软雅黑,宋体"}},
    "grid": {"x": 43, "x2": 20, "y": 50, "borderWidth": 0, "y2": 33},
    "dataZoom": {"show": false, "realtime": true, "start": 0, "end": 100},
    "yAxis": [{
        "type": "value",
        "splitArea": {"show": true},
        "axisLabel": {"textStyle": {"fontFamily": "simhei"}},
        "splitNumber": 5
    }],
    "xAxis": {
        "type": "category",
        "boundaryGap": true,
        "axisLine": {"onZero": false},
        "data": ["谷歌", "腾讯搜搜", "网易有道", "狗狗搜索", "搜酷", "搜狗", "宜搜", "奇虎搜索", "即刻搜索"]
    }
}