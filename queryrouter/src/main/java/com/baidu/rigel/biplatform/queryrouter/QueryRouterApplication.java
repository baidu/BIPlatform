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
package com.baidu.rigel.biplatform.queryrouter;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.parser.RegisterFunction;
import com.baidu.rigel.biplatform.parser.exception.RegisterFunctionException;
import com.baidu.rigel.biplatform.queryrouter.query.udf.DateDataFunction;
import com.baidu.rigel.biplatform.queryrouter.query.udf.RelativeRate;
import com.baidu.rigel.biplatform.queryrouter.query.udf.SimilitudeRate;

/**
 * 
 * 平台服务入口 提供脱离tomcat容器提供queryservice的能力
 * 
 * @author 罗文磊
 * @version 1.0.0.1
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
        RedisAutoConfiguration.class })
@ImportResource({ "applicationContext-queryrouter.xml"})
public class QueryRouterApplication extends SpringBootServletInitializer {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(QueryRouterApplication.class);
    
    /*
     * 设置gzip压缩
     */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addConnectorCustomizers (customizer());
        return tomcat;
    }
    
    @Bean
    public TomcatConnectorCustomizer customizer() {
        return new TomcatConnectorCustomizer() {
            
            @Override
            public void customize(Connector connector) {
                connector.setAttribute("socket.directBuffer", true);
                // nio2在第三方应用中存在问题
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                protocol.setMaxThreads(1000);
                protocol.setMinSpareThreads(100);
                protocol.setMaxConnections(700);
            }
        };
    }
    
    /**
     * 程序入口
     * 
     * @param args
     *            外部参数
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(
                QueryRouterApplication.class, args);
        try {
            RegisterFunction.register("rRate", RelativeRate.class);
            RegisterFunction.register("sRate", SimilitudeRate.class);
            RegisterFunction.register("dateData", DateDataFunction.class);
        } catch (RegisterFunctionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ApplicationContextHelper.setContext(context);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(QueryRouterApplication.class);
    }
    
}
