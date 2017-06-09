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
package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;

/**
 *Description: 图形格式化设置
 * @author david.wang
 *
 */
public class ChartFormatModel implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6463353155721195310L;
    
    /**
     * 个性化设置
     */
    private PersonalSetting setting = null;
    
    /**
     * 外观设置
     */
    private AppearanceModel appearance;

    /**
     * @return the setting
     */
    public PersonalSetting getSetting() {
        if (setting == null) {
            setting = new PersonalSetting ();
        }
        return setting;
    }

    /**
     * @param setting the setting to set
     */
    public void setSetting(PersonalSetting setting) {
        this.setting = setting;
    }

    /**
     * @return the appearance
     */
    public AppearanceModel getAppearance() {
        if (appearance == null) {
            this.appearance = new AppearanceModel();
        }
        return appearance;
    }

    /**
     * @param appearance the appearance to set
     */
    public void setAppearance(AppearanceModel appearance) {
        this.appearance = appearance;
    }
    
    
}
