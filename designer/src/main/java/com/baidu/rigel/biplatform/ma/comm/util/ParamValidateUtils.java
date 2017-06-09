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
package com.baidu.rigel.biplatform.ma.comm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 
 * utility class : validate the method input parameters is null or not
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public final class ParamValidateUtils {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(ParamValidateUtils.class);
    
    /**
     * 
     * ParamValidateUtils
     */
    private ParamValidateUtils() {
    }
    
    /**
     * check the method param's 
     * @param name -- the method input parameter's name
     * @param value -- the parameter's value
     * @return boolean -- if invalidate return false else true
     */
    public static boolean check(String name, Object value) {
        if (name == null) {
            return false;
        }
        if (value == null) {
            logger.error("params {} can't not be null", name);
            return false;
        } else  if (value instanceof Object[] && ((Object[]) value).length == 0) {
            logger.error("params {} is empty", name);
            return false;
        } else if (StringUtils.isEmpty(value)) {
            logger.error("params {} can't not be null", name);
            return false;
        }
        return true;
    }
}
