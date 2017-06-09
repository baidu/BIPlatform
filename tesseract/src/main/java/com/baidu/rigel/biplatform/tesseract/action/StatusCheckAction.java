
/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.tesseract.action;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.ac.util.ResponseResultUtils;

/** 
 * 健康检查专用，检测业务存活性。以后可能扩展
 * @author xiaoming.chen
 * @version  2014年12月2日 
 * @since jdk 1.8 or after
 */
@RestController
public class StatusCheckAction {
    
    
    /** 
     * 校验系统状态
     * checkStatus
     * @return
     */
    @RequestMapping(value = "/checkStatus")
    public ResponseResult checkStatus() {
        return ResponseResultUtils.getCorrectResult("OK", null);
    }

}

