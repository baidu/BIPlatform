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
package com.baidu.rigel.biplatform.ma.resource;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 该controler负责返回springmvc默认的异常页面
 * 
 * @author majun04
 *
 */
@Controller
public class MainsiteErrorController implements ErrorController {
	/**
	 * springboot默认的异常捕获处理路径，需自己实现自定义的返回页面
	 */
	private static final String ERROR_PATH = "/error";

	/**
	 * 当系统出现404或者500等错误时，应该展示的共有错误页面
	 * 
	 * @return
	 */
	@RequestMapping(value = ERROR_PATH)
	public ModelAndView handleError() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("/page/error");
		return mv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.boot.autoconfigure.web.ErrorController#getErrorPath()
	 */
	@Override
	public String getErrorPath() {
		return ERROR_PATH;
	}

}