package com.starix.gdou.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置同时支持HTTP和HTTPS请求
 * 或者使HTTP请求重定向到HTTPS
 * @author Starix
 * @date 2019-12-29 14:42
 */
@Configuration
public class ConnectorConfig {

    @Value("${server.custom.httpPort}")
    private int httpPort;

    // @Value("${server.port}")
    // private int httpsPort;

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        //强制使用https并使http转向https配置
        // TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory() {
        //     @Override
        //     protected void postProcessContext(Context context) {
        //         SecurityConstraint securityConstraint = new SecurityConstraint();
        //         securityConstraint.setUserConstraint("CONFIDENTIAL");
        //         SecurityCollection securityCollection = new SecurityCollection();
        //         securityCollection.addPattern("/*");
        //         securityConstraint.addCollection(securityCollection);
        //         context.addConstraint(securityConstraint);
        //     }
        // };
        // factory.addAdditionalTomcatConnectors(redirectConnector());

        // 添加http
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addAdditionalTomcatConnectors(createStandardConnector());
        return factory;
    }

    // 配置http
    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }

    //http重定向到https
    // private Connector redirectConnector() {
    //     Connector connector = new Connector(Http11NioProtocol.class.getName());
    //     connector.setScheme("http");
    //     connector.setPort(serverPortHttp);
    //     connector.setSecure(false);
    //     connector.setRedirectPort(serverPortHttps);
    //     return connector;
    // }
}
