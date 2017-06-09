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
package com.baidu.rigel.biplatform.tesseract.application;

import java.util.function.Function;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.parser.RegisterFunction;
import com.baidu.rigel.biplatform.parser.exception.RegisterFunctionException;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.DateDataFunction;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.RelativeRate;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.SimilitudeRate;

/**
 * Tesseract项目启动入口
 * 
 * @author xiaoming.chen
 *
 */
@Configuration
@ComponentScan(basePackages = "com.baidu.rigel.biplatform.tesseract")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class})
@ImportResource({"conf/applicationContext-cache.xml","conf/applicationContext-tesseract.xml"})
public class TesseractApplication {

    /**
     * 启动Tesseract
     * 
     * @param args 启动参数
     * @throws RegisterFunctionException 
     */
    public static void main(String[] args) throws RegisterFunctionException {

        ConfigurableApplicationContext  context = SpringApplication.run(TesseractApplication.class);
        
        ApplicationContextHelper.setContext(context);
        
        RegisterFunction.register("rRate", RelativeRate.class);
        RegisterFunction.register("sRate", SimilitudeRate.class);
        RegisterFunction.register("dateData", DateDataFunction.class);
        
        StoreManager.addUdfDeSerializerSetting (new Function<Object[], Object>() {
            
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Object apply(Object[] t) {
                try {
                    return AnswerCoreConstant.GSON.fromJson (t[0].toString (), (Class)t[1]);
                } catch (Exception e) {
                    return null;
                }
            }
        });
        StoreManager.addUdfSerializerSetting (new Function<Object[], Object>() {
            public Object apply(Object[] t) {
                return AnswerCoreConstant.GSON.toJson (t[0]);
            }
        });
        
//        CacheManager cacheManager = (CacheManager) context.getBean("redisCacheManager");
//        
//        cacheManager.getCache("test").put("key", "val");
    }
    
    @Bean
    public EmbeddedServletContainerFactory servletContainer () {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory ();
        tomcat.setProtocol ("org.apache.coyote.http11.Http11Nio2Protocol");
        tomcat.addConnectorCustomizers (customizer());
        return tomcat;
    }

    @Bean
    public TomcatConnectorCustomizer customizer () {
        return new TomcatConnectorCustomizer() {
            
            @Override
            public void customize(Connector connector) {
                connector.setAttribute ("socket.directBuffer", true);
                Http11Nio2Protocol protocol = (Http11Nio2Protocol) connector.getProtocolHandler ();
                protocol.setMaxThreads (1000);
                protocol.setMinSpareThreads (75);
                protocol.setAcceptorThreadPriority (10);
                protocol.setPollerThreadPriority (10);
                protocol.setMaxKeepAliveRequests (100);
            }
        };
    }
    
}